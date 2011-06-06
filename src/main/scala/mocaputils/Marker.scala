package mocaputils

import scala.collection.immutable._
import scala.math.sqrt
import signal.{ Butter, FiltFilt, PSD, Detrend }
import Detrend.detrend
import mocaputils.collection.immutable.RichSeq

/** A marker (without any gaps). */
trait Marker { self =>
  
  /** Name of the marker. */
  val name: String
  /** Marker coordinates. */
  val co: IndexedSeq[(Double, Double, Double)]
  /** Sample frequency (Hz). */
  val fs: Double
  
  /** `x`-values of the marker coordinates */
  lazy val xs: IndexedSeq[Double] = co.map(_._1)
  /** `y`-values of the marker coordinates */
  lazy val ys: IndexedSeq[Double] = co.map(_._2)
  /** `z`-values of the marker coordinates */
  lazy val zs: IndexedSeq[Double] = co.map(_._3)
  
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
      val (dx, dy, dz) = (x2._1 - x1._1, x2._2 - x1._2, x2._3 - x1._3)
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
  def butter2(fc: Double): Marker = new Marker {
    val name = self.name
    val fs = self.fs
    private val sos = Butter.butterSOSEven(2, fc / (fs / 2)).head
    private val b = List(sos.b0, sos.b1, sos.b2)
    private val a = List(1, sos.a1, sos.a2)
    override lazy val xs = FiltFilt.filtfilt(b, a, self.xs)
    override lazy val ys = FiltFilt.filtfilt(b, a, self.ys)
    override lazy val zs = FiltFilt.filtfilt(b, a, self.zs)
    assert(xs.length == ys.length)
    assert(xs.length == zs.length)
    override val co = new IndexedSeq[(Double, Double, Double)] {
      val length = xs.length
      def apply(item: Int) = (xs(item), ys(item), zs(item))
    }
  }
  
}
