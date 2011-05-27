package mocaputils.plotting

import scala.collection.immutable._

/** `XYSeries` that is sampled in time.
 * 
 *  @param name name of the series
 *  @param x time samples
 *  @param fs sampling frequency */
case class TimeSampledSeries(
  name: String, 
  x: IndexedSeq[Double], 
  fs: Double = 1) 
extends XYSeries {
  val length = x.length
  def apply(item: Int) = (fs * item, x(item))
}