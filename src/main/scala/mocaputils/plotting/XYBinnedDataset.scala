package mocaputils.plotting

import scala.collection.immutable._
import org.jfree.data.xy.IntervalXYDataset

/** Implementation of an `org.jfree.data.xy.IntervalXYDataset`. */
trait XYBinnedDataset extends XYDataset with IntervalXYDataset {
  val series: IndexedSeq[BinnedXYSeries]
  override def getStartX(s: Int, item: Int) = series(s).getStartX(item)
  override def getStartXValue(s: Int, item: Int) = getStartX(s, item)
  override def getEndX(s: Int, item: Int) = series(s).getEndX(item)
  override def getEndXValue(s: Int, item: Int) = getEndX(s, item)
  override def getStartY(s: Int, item: Int) = series(s).getStartY(item)
  override def getStartYValue(s: Int, item: Int) = getStartY(s, item)
  override def getEndY(s: Int, item: Int) = series(s).getEndY(item)
  override def getEndYValue(s: Int, item: Int) = getEndY(s, item)
}