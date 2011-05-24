package mocaputils

import scala.collection.immutable._
import mocaputils.collection.immutable.RichSeq

/** Marker that may have gaps in its `(x,y,z)` coordinates.
 *  
 *  Markers read from a motion capture file may have gaps in their data, which
 *  are due to markers being occluded from enough cameras that reconstruction
 *  is not possible.  This trait represents markers of that kind, and wraps
 *  their coordinates in `Option`s.  A `None` represents missing data, while a
 *  `Some` represents a marker that does exist.
 *
 *  @author Jonathan Merritt <merritt@unimelb.edu.au> */
trait GappedMarker {

  /** Name of the marker. */
  val name: String

  /** Coordinates of the marker. */
  val co: IndexedSeq[Option[(Double, Double, Double)]]

  /** Sample frequency (Hz). */
  val fs: Double
  
  /** Range of frames (inclusive) over which the marker is defined. */
  lazy val range: (Int, Int) = 
    (co.indexWhere(_.isDefined), co.lastIndexWhere(_.isDefined)) 
  
  /** `x`-values of the marker coordinates. */
  lazy val xs: IndexedSeq[Option[Double]] = co.map(_.map(_._1))
  /** `y`-values of the marker coordinates. */
  lazy val ys: IndexedSeq[Option[Double]] = co.map(_.map(_._2))
  /** `z`-values of the marker coordinates. */
  lazy val zs: IndexedSeq[Option[Double]] = co.map(_.map(_._3))
  
  /** Sequence of gaps in the marker data (slices in which the coordinates
   *  are not defined). */
  lazy val gaps: Seq[(Int, Int)] = RichSeq(co).slicesWhere(!_.isDefined)
}
