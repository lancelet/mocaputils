package mocaputils

import java.io.IOException
import collection.immutable._
import io.Source
import scalaz.{ Validation, Success, Failure }

/** Force plate data from a `*.forces` file. */
trait ForcePlate {
  /** Range of frames over which the force data is defined. */
  val range: (Int, Int)
  /** Obtain force at a specific frame. */
  def getForce(frame: Int): (Double, Double, Double)
  /** Get COP at a specific frame. */
  def getCOP(frame: Int): (Double, Double, Double)
  /** Get Mz at a specific frame. */
  def getMz(frame: Int): Double

  /** IndexedSeq over items that are defined for the whole range. 
   *  This is used to obtain the sequences below. */
  private def rangeSeq[T](fetch: Int => T) = 
    (range._1 to range._2).map(fetch(_)).toIndexedSeq

  /** `IndexedSeq` for force values. */
  lazy val forceSeq: IndexedSeq[(Double, Double, Double)] = 
    rangeSeq(getForce)
  /** `IndexedSeq` for COP values. */
  lazy val copSeq: IndexedSeq[(Double, Double, Double)] = rangeSeq(getCOP)
  /** `IndexedSeq` for Mz values. */
  lazy val mzSeq: IndexedSeq[Double] = rangeSeq(getMz)
}

/** Data from a `*.forces` file. */
trait Forces {
  /** Force plates. */
  val plates: Seq[ForcePlate]
  /** Number of force plates. */
  val nPlates: Int
  /** Sample rate. */
  val sampleRate: Double
  /** Number of samples. */
  val numSamples: Int

  /** Gets a single force plate. */
  def getPlate(index: Int): ForcePlate = plates(index)
}

/** Reader of `*.forces` files. */
object ForceReader {

  /** Reads a `*.forces` file from a specified file name.
    *
    * This method shouldn't throw an exception - any exceptions are a bug to be
    * fixed.
    * 
    * @param name name of the file to read
    * @return data from the file, or an error message */
  def read(fileName: String): Validation[String, Forces] = {
    try {
      read(Source.fromFile(fileName).getLines)
    } catch {
      case ioe: IOException => Failure(ioe.getMessage)
    }
  }

  /** Reads a `*.forces` file from an `Iterator[String]` containing the lines of
    * the file.
    *
    * This method shouldn't throw an exception - any exceptions are a bug to be
    * fixed.
    *
    * @param inputLines lines of the file
    * @return data from the file, or an error message */
  def read(inputLines: Iterator[String]): Validation[String, Forces] = {

    import scala.{ None => N }
    import ReadUtils._
    implicit def stringToSome(s: String): Some[String] = Some(s)

    // first line: "[Force Data]"
    fetchLineAndCheck(inputLines, "[Force Data]").fold(
      e => return Failure(e),
      s => s
    )

    // second line: number of force plates
    val nPlates_ = getSingleIntField(inputLines, "NumberOfForcePlates").fold(
      e => return Failure(e),
      s => s
    )

    // third line: sample rate
    val sampleRate_ = getSingleDoubleField(inputLines, "SampleRate").fold(
      e => return Failure(e),
      s => s
    )

    // fourth line: number of samples
    val numSamples_ = getSingleIntField(inputLines, "NumberOfSamples").fold(
      e => return Failure(e),
      s => s
    )

    // skip force plate labels line
    skipLines(inputLines, 1) match {
      case Some(e) => return Failure(e)
      case None => { }
    }

    // read force, position and moment values from the rest of the file
    val groupedValues = for (line <- inputLines) yield {
      val ords = line.split("\t", -1).drop(1).take(nPlates_ * 7).map(_.trim.toDouble)
      assert(ords.length == nPlates_ * 7)
      ords.grouped(7).toIndexedSeq
    }
    val plateValues = groupedValues.toIndexedSeq.transpose

    // convert force data to ForcePlate traits
    assert(nPlates_ == plateValues.size)
    val plates_ = for (i <- 0 until nPlates_) yield {
      new ForcePlate {
	override val range = (0, plateValues.size)
	override def getForce(frame: Int) = {
	  val v = plateValues(i)(frame)
	  (v(0), v(1), v(2))
	}
	override def getCOP(frame: Int) = {
	  val v = plateValues(i)(frame)
	  (v(3), v(4), v(5))
	}
	override def getMz(frame: Int) = plateValues(i)(frame)(6)
      }
    }

    // convert result to a Forces trait
    Success(
      new Forces {
	override val plates = plates_
	override val nPlates = nPlates_
	override val sampleRate = sampleRate_
	override val numSamples = numSamples_
      }
    )
  }

}
