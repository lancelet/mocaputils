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
  def name: String

  /** Coordinates of the marker. */
  def co: IndexedSeq[Option[Vec3]]

  /** Whether the marker exists - it exists if some coordinates are present. */
  def exists: Boolean
  
  /** Sample frequency (Hz). */
  def fs: Double
  
  /** Range of frames (inclusive) over which the marker is defined. */
  def range: (Int, Int) 
  
  /** `x`-values of the marker coordinates. */
  def xs: IndexedSeq[Option[Double]]
  /** `y`-values of the marker coordinates. */
  def ys: IndexedSeq[Option[Double]]
  /** `z`-values of the marker coordinates. */
  def zs: IndexedSeq[Option[Double]]
  
  /** Sequence of gaps in the marker data (slices in which the coordinates
   *  are not defined). */
  def gaps: Seq[(Int, Int)]
  
  /** Take a sub-set of the marker's coordinates.
    * 
    * This creates a new GappedMarker, in which the new index zero is 
    * `firstIndex` from this marker.  `untilIndex-1` is the last index
    * included (the same syntax as `0 until 5`).
    * 
    * @param firstIndex index to slice from (becomes index 0 in the new
    *   marker).
    * @param untilIndex limiting index.
    * 
    * @return new gapped marker sliced from the required range
    */
  def slice(firstIndex: Int, untilIndex: Int): GappedMarker
  
}

object GappedMarker {

  /** Template trait for GappedMarkers. */
  trait GappedMarkerLike extends GappedMarker {
    lazy val exists: Boolean = co.exists(_.isDefined)
    lazy val range: (Int, Int) = 
      (co.indexWhere(_.isDefined), co.lastIndexWhere(_.isDefined))
    lazy val gaps: Seq[(Int, Int)] = RichSeq(co).slicesWhere(!_.isDefined)
    def slice(firstIndex: Int, untilIndex: Int): GappedMarker =
      Vec3GappedMarker(name, co.slice(firstIndex, untilIndex), fs)
  }
  
  /** Trait for GappedMarkers which have co, and need to form separate x,
    * y and z. */
  trait XYZFromCo {
    def co: IndexedSeq[Option[Vec3]]
    
    val xs: IndexedSeq[Option[Double]] = IndexedSeqX
    val ys: IndexedSeq[Option[Double]] = IndexedSeqY
    val zs: IndexedSeq[Option[Double]] = IndexedSeqZ
    
    sealed private trait EmbeddedIndexSeq extends IndexedSeq[Option[Double]] {
      def length: Int = co.length
    }
    private object IndexedSeqX extends EmbeddedIndexSeq {
      def apply(index: Int): Option[Double] = co(index).map(_.x)
    }
    private object IndexedSeqY extends EmbeddedIndexSeq {
      def apply(index: Int): Option[Double] = co(index).map(_.y)
    }
    private object IndexedSeqZ extends EmbeddedIndexSeq {
      def apply(index: Int): Option[Double] = co(index).map(_.z)
    }
  }
  
  /** GappedMarker which is defined in terms of an IndexedSeq of Option[Vec3].
    * */
  final case class Vec3GappedMarker (
    name: String,
    co: IndexedSeq[Option[Vec3]],
    fs: Double
  ) extends GappedMarkerLike with XYZFromCo
  
}
