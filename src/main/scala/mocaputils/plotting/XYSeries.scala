package mocaputils.plotting

import scala.collection.immutable._

/** XY Series.
 * 
 *  An `XYSeries` is an `IndexedSeq[(Double, Double)]` which provides
 *  `(x, y)` data for plotting.  The name of the data set must also be
 *  supplied.
 *  
 *  For example, an `XYSeries` may be constructed from a `Vector` of
 *  values as follows:
 *  {{{
 *  import scala.collection.immutable._
 *  val v: Vector[(Double, Double)] = { /* ... */ }
 *  val xySeries = new XYSeries {
 *    val name = "My XYSeries"
 *    val length = xySeries.length
 *    def apply(item: Int) = v(item)
 *  }
 *  }}}
 */
trait XYSeries extends IndexedSeq[(Double, Double)] with Series {
  def getX(item: Int) = apply(item)._1
  def getY(item: Int) = apply(item)._2
}
