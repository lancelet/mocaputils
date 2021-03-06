package mocaputils

import scala.collection.immutable._
import scala.math.sqrt
import signal.{ Butter, FiltFilt, PSD, Detrend }
import Detrend.detrend
import mocaputils.collection.immutable.RichSeq

/** A marker (without any gaps). */
trait Marker { self =>
  
  import Marker._
  
  /** Name of the marker. */
  def name: String
  /** Marker coordinates. */
  def co: IndexedSeq[Vec3]
  /** Sample frequency (Hz). */
  def fs: Double
  
  /** `x`-values of the marker coordinates */
  def xs: IndexedSeq[Double]// = Vec3IndexedSeqX(co)
  /** `y`-values of the marker coordinates */
  def ys: IndexedSeq[Double]// = Vec3IndexedSeqY(co)
  /** `z`-values of the marker coordinates */
  def zs: IndexedSeq[Double]// = Vec3IndexedSeqZ(co)
  
  /** Estimate of the bandwidth of the marker. 
   * 
   *  The bandwidth is defined as the frequency below which `powerFraction`
   *  amount of the power in the marker signal is contained.  The power
   *  spectral density (PSD) of the `x`, `y` and `z` coordinates of the
   *  marker are found using the spectrogram method.  The bandwidth of
   *  each coordinate is computed separately.  Then, the maximum bandwidth
   *  is returned.
   *  
   *  @param powerFraction the fraction of power contained within the
   *    returned bandwidth
   *  @return bandwidth of the marker (frequency below which `powerFraction`
   *    amount of the total signal power is contained) */
  def bandwidth(powerFraction: Double = 0.95): Double = {
    val xbw = PSD.bandwidth(PSD.psd(detrend(xs), fs), powerFraction)
    val ybw = PSD.bandwidth(PSD.psd(detrend(ys), fs), powerFraction)
    val zbw = PSD.bandwidth(PSD.psd(detrend(zs), fs), powerFraction)
    List(xbw, ybw, zbw).max
  }

  /** Finds regions of discontinuity in the marker coordinate data.
   *  
   *  The vector difference between successive marker coordinates is
   *  computed.  Regions in which the magnitude of this difference is
   *  larger than `threshold` are returned.
   *  
   *  @param threshold threshold for discontinuities
   *  @return sequence of indices over which discontinuities occur */
  def discontinuities(threshold: Double): Seq[(Int, Int)] = {
    // compute deltas between pairs of coordinates
    val deltas = for ((x1, x2) <- co.dropRight(1) zip co.drop(1)) yield {
      val (dx, dy, dz) = (x2.x - x1.x, x2.y - x1.y, x2.z - x1.z)
      sqrt(dx * dx + dy * dy + dz * dz)
    }    
    // find regions where deltas are > threshold
    RichSeq(deltas).slicesWhere(_ > threshold)
  }
  
  /** Applies a second-order forward-reverse low-pass Butterworth filter to
   *  the marker data.
   *  
   *  @param fc cutoff frequency
   *  @return new marker with filtered coordinates */
  def butter2(fc: Double): Marker = {
    // pull Butterworth coefficients from a single second-order-section filter
    val sos = Butter.butterSOSEven(2, fc / (fs / 2)).head
    val b = List(sos.b0, sos.b1, sos.b2)
    val a = List(1, sos.a1, sos.a2)
    val xs = FiltFilt.filtfilt(b, a, self.xs)
    val ys = FiltFilt.filtfilt(b, a, self.ys)
    val zs = FiltFilt.filtfilt(b, a, self.zs)
    assert(xs.length == ys.length)
    assert(xs.length == zs.length)
    XYZMarker(self.name, self.fs, xs, ys, zs)
  }
  
}

object Marker {
  
  /** Trait for markers which have separate x, y and z coordinates, and need
    * to form the co: IndexedSeq[Vec3] coordinates. */
  trait CoFromXYZ {
    def xs: IndexedSeq[Double]
    def ys: IndexedSeq[Double]
    def zs: IndexedSeq[Double]

    val co: IndexedSeq[Vec3] = XYZCo(xs, ys, zs)

    private case class XYZCo(
      xs: IndexedSeq[Double],
      ys: IndexedSeq[Double],
      zs: IndexedSeq[Double]
    ) extends IndexedSeq[Vec3] {
      assert(xs.length == ys.length && xs.length == zs.length)
      def length: Int = xs.length
      def apply(index: Int): Vec3 = WrappedVec3(index)
      private case class WrappedVec3(index: Int) extends Vec3 with Vec3.Ops {
        def x: Double = xs(index)
        def y: Double = ys(index)
        def z: Double = zs(index)
      }
    }  
  }

  /** Trait for markers which have co, and need to form separate x, y and z
    * coordinates. */
  trait XYZFromCo {
    def co: IndexedSeq[Vec3]
    
    val xs: IndexedSeq[Double] = IndexedSeqX
    val ys: IndexedSeq[Double] = IndexedSeqY
    val zs: IndexedSeq[Double] = IndexedSeqZ
    
    sealed private trait EmbeddedIndexSeq extends IndexedSeq[Double] {
      def length: Int = co.length
    }
    private object IndexedSeqX extends EmbeddedIndexSeq {
      def apply(index: Int): Double = co(index).x
    }
    private object IndexedSeqY extends EmbeddedIndexSeq {
      def apply(index: Int): Double = co(index).y
    }
    private object IndexedSeqZ extends EmbeddedIndexSeq {
      def apply(index: Int): Double = co(index).z
    }        
  }
  
  /** Marker defined in terms of separate x, y and z coordinates. */
  final case class XYZMarker(
    name: String,
    fs: Double,
    xs: IndexedSeq[Double],
    ys: IndexedSeq[Double],
    zs: IndexedSeq[Double]
  ) extends Marker with CoFromXYZ
  
  /** Marker defined in terms of an IndexedSeq[Vec3]. */
  final case class Vec3Marker(
    name: String,
    fs: Double,
    co: IndexedSeq[Vec3]
  ) extends Marker with XYZFromCo
  
}
