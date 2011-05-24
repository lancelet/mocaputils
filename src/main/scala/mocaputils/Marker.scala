package mocaputils

import scala.collection.immutable._
import signal.PSD

/** A marker (without any gaps). */
trait Marker {
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
  
  /** Estimate of the bandwidth of the marker. */
  def bandwidth(powerFraction: Double = 0.95): Double = {
    val xbw = PSD.bandwidth(PSD.psd(xs, fs), powerFraction)
    val ybw = PSD.bandwidth(PSD.psd(ys, fs), powerFraction)
    val zbw = PSD.bandwidth(PSD.psd(zs, fs), powerFraction)
    List(xbw, ybw, zbw).max
  }
}