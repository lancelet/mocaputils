package mocaputils

import java.io.IOException
import scala.collection.breakOut
import scala.collection.immutable._
import scala.collection.mutable.ArrayBuffer
import io.Source
import scalaz.{ Validation, Success, Failure }

/** TRC data.
  * 
  * Trait for accessing data contained in a TRC file. */
trait TRCData {
  /** Markers in the TRC data. */
  val markers: IndexedSeq[GappedMarker]
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
    val groupedCoords: ArrayBuffer[ArrayBuffer[Option[Vec3]]] =
      ArrayBuffer()
    for (line <- inputLines) {
      val ords = line.split("\t", -1).drop(2).take(nm * 3).map(_.trim)
      assert(ords.length == nm * 3)
      val coordLine: ArrayBuffer[Option[Vec3]] = ArrayBuffer()
      for (o3 <- ords.grouped(3)) {
        val appendValue = if (o3.exists(_.isEmpty)) {
          None
        } else {
          val d = o3.map(_.toDouble)
          Some(Vec3(d(0), d(1), d(2)))
        }
        coordLine += appendValue
      }
      groupedCoords += coordLine
    }
    val markerCoords = groupedCoords.transpose
    
    /*
    type D3 = (Double, Double, Double)
    val groupedCoords: IndexedSeq[IndexedSeq[Option[D3]]] =
      inputLines.map(line => {
        val ords = line.split("\t", -1).drop(2).take(nm * 3).map(_.trim)
        assert(ords.length == nm * 3)
        val optionCoords: IndexedSeq[Option[D3]] =
          (for (o3 <- ords.grouped(3)) yield {
            if (o3.exists(_.isEmpty)) None
            else {
              val d = o3.map(_.toDouble)
              Some((d(0), d(1), d(2)))
            }
          }).toIndexedSeq
      })(breakOut)
    val markerCoords = groupedCoords.transpose
    */

    // convert marker coordinates to markers
    assert(markerNames.size == markerCoords.size)
    val markers: IndexedSeq[GappedMarker] = 
      (for ((n, c) <- markerNames zip markerCoords) yield
         GappedMarkerCase(n, c.toIndexedSeq, cr))(breakOut)

    // return the TRC data
    Success(TRCDataCase(markers, fileName, dr, cr, nf, nm, u, or, os, on))
  }
  
  private final case class GappedMarkerCase(
    name: String,
    co: IndexedSeq[Option[Vec3]],
    fs: Double
  ) extends GappedMarker
  
  private final case class TRCDataCase(
    markers: IndexedSeq[GappedMarker],
    internalFileName: String,
    dataRate: Double,
    cameraRate: Double,
    numFrames: Int,
    numMarkers: Int,
    units: String,
    origDataRate: Double,
    origDataStartFrame: Int,
    origNumFrames: Int
  ) extends TRCData {
    private val markerMap: Map[String, GappedMarker] =
      markers.map(x => (x.name, x))(breakOut)
    override def getMarker(name: String) = markerMap(name)
  }

}
