package mocaputils

import java.io.IOException
import collection.immutable._
import io.Source
import scalaz.{ Validation, Success, Failure }

/** TRC data.
  * 
  * Trait for accessing data contained in a TRC file. */
trait TRCData {
  /** Markers in the TRC data. */
  val markers: Seq[GappedMarker]
  /** Name of the original TRC file, as specified by the file itself. */
  val internalFileName: String
  /** Data rate (Hz). */
  val dataRate: Double
  /** Camera rate (Hz). */
  val cameraRate: Double
  /** Number of frames stored in the file. */
  val numFrames: Int
  /** Number of markers stored in the file. */
  val numMarkers: Int
  /** Units of the file (eg. "mm"). */
  val units: String
  /** Original data rate. */
  val origDataRate: Double
  /** Original data start frame. */
  val origDataStartFrame: Int
  /** Original number of frames. */
  val origNumFrames: Int

  /** Gets a single marker. */
  def getMarker(name: String): GappedMarker = 
    markers.filter(_.name == name).head
}

/** Reader of TRC files. */
object TRCReader {

  /** Reads a TRC file from a specified file name.
    *
    * This method shouldn't throw an exception - any exceptions are a bug to be
    * fixed.
    *
    * @param name name of the file to read
    * @return data from the file, or an error message */
  def read(fileName: String): Validation[String, TRCData] = {
    try {
      read(Source.fromFile(fileName).getLines)
    } catch {
      case ioe: IOException => Failure(ioe.getMessage)
    }
  }

  /** Reads a TRC file from an `Iterator[String]` containing the lines of the
    * file.
    *
    * This method shouldn't throw an exception - any exceptions are a bug to be
    * fixed.
    * 
    * @param inputLines lines of the file
    * @return data from the file, or an error message */
  def read(inputLines: Iterator[String]): Validation[String, TRCData] = {

    import scala.{ None => N }
    import ReadUtils._
    implicit def stringToSome(s: String): Some[String] = Some(s)

    // first line: file name
    val fileName = fetchLineAndCheck(inputLines, "PathFileType", "4", 
        "(X/Y/Z)", N).fold(
      e => return Failure(e),
      s => s(3)
    )

    // second line: just check expected fields
    fetchLineAndCheck(inputLines, "DataRate", "CameraRate", "NumFrames",
		      "NumMarkers", "Units", "OrigDataRate",
		      "OrigDataStartFrame", "OrigNumFrames").fold(
      e => return Failure(e),
      s => s
    )

    // third line: read dataRate, etc...
    val (dr, cr, nf, nm, u, or, os, on) =
      fetchLineAndCheck(inputLines, N, N, N, N, N, N, N, N).fold(
	e => return Failure(e),
	s => {
	  val items = for (
	    dr <- getDouble(s, 0);
	    cr <- getDouble(s, 1);
	    nf <- getInt(s, 2);
	    nm <- getInt(s, 3);
	    or <- getDouble(s, 5);
	    os <- getInt(s, 6);
	    on <- getInt(s, 7)
	  ) yield (dr, cr, nf, nm, s(4), or, os, on)
	  items.fold(
	    e => return Failure(e),
	    s => s
	  )
	}
      )

    // fourth line: marker names
    val markerNames = nextLine(inputLines).fold(
      e => return Failure(e),
      s => s.split("\t").drop(2).filterNot(_.trim.isEmpty)
    )

    // skip 5th and 6th lines
    skipLines(inputLines, 2) match {
      case Some(e) => return Failure(e)
      case None => { }
    }

    // read coordinate lines from the remainder of the file
    val groupedCoords = for (line <- inputLines) yield {
      val ords = line.split("\t", -1).drop(2).take(nm * 3).map(_.trim)
      assert(ords.length == nm * 3)
      val ordsWrappedInOption = ords.grouped(3).map( x =>
	if (x.exists(_.isEmpty)) {
	  None
	} else {
	  val d = x.map(_.toDouble)
	  Some((d(0), d(1), d(2)))
	}
      )
      ordsWrappedInOption.toIndexedSeq
    }
    val markerCoords = groupedCoords.toIndexedSeq.transpose

    // convert marker coordinates to markers
    assert(markerNames.size == markerCoords.size)
    val markers_ = for ((n, c) <- markerNames zip markerCoords) yield {
      new GappedMarker {
        override val name = n
        override val co = c
      }
    }

    // return the TRC data
    Success(
      new TRCData {
	override val markers = markers_.toIndexedSeq
	override val internalFileName = fileName
	override val dataRate = dr
	override val cameraRate = cr
	override val numFrames = nf
	override val numMarkers = nm
	override val units = u
	override val origDataRate = or
	override val origDataStartFrame = os
	override val origNumFrames = on

	private val markerMap: Map[String, GappedMarker] = Map() ++ 
	  (markers.map(x => (x.name, x)))
	override def getMarker(name: String) = markerMap(name)
      }
    )
  }

}
