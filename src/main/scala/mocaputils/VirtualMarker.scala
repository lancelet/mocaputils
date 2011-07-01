package mocaputils

import scala.collection.immutable._
import mocaputils.transforms.Veldpaus.veldpaus

/** Virtual marker; tracking a set of other markers on a rigid body.
 *  
 *  The virtual marker tracks a set of markers which are presumed to move
 *  following rigid body motion.  The transformation of the reference markers
 *  is found using the Veldpaus least-squares method.
 *  
 *  A reference position for the virtual marker must be given as `refPos`.
 *  This virtual marker reference position is accompanied by a set of
 *  reference positions of real markers, given by `refMarkers.unzip._1`.  The
 *  reference positions must all exist in the same coordinate system.
 *  As the markers move, their coordinates are then required in order to
 *  compute the position of the virtual marker.  To achieve this, the
 *  markers themselves are also supplied, in `refMarkers.unzip._2`.
 *  
 *  @param name name of the virtual marker
 *  @param refPos reference position of the virtual marker
 *  @param refMarkers a sequence of reference positions for the tracked
 *    markers and the markers themselves */
class VirtualMarker(
  val name: String,
  refPos: (Double, Double, Double),
  refMarkers: Seq[((Double, Double, Double), Marker)]
) extends Marker
{
  private val (ref, markers) = refMarkers.unzip
  
  // must have at least 3 markers
  require(refMarkers.length >= 3)
  // all markers must have the same sampling frequency
  val fs: Double = markers.head.fs
  require(markers.forall(_.fs == fs))
  // all markers must have the same length
  private val nSamples = markers.head.co.length
  require(markers.forall(_.co.length == nSamples))

  // compute coordinates of the marker.  the logic here is to find the
  //  transformation: T(ref -> i), where ref is the reference position and i
  //  is the sample index at which the marker positions can be evaluated.  
  //  this transformation is then applied to the reference position of the 
  //  marker, to find its position at sample i: 
  //    virtualMarker(i) = T(ref -> i) * refPos
  val co = for {
    i <- 0 until nSamples
    currentPos = markers.map(_.co(i))
  } yield veldpaus(ref zip currentPos)(refPos)
}
