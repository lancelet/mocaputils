package mocaputils.plotting

import scala.collection.immutable._
import org.jfree.data.xy.{ XYDataset => JFreeXYDataset }
import org.jfree.data.DomainOrder

trait XYDataset extends JFreeXYDataset with SeriesDataset {
  val series: IndexedSeq[XYSeries]
  override def getDomainOrder() = DomainOrder.ASCENDING
  override def getItemCount(s: Int) = series(s).length
  override def getX(s: Int, item: Int) = series(s).getX(item)
  override def getXValue(s: Int, item: Int) = series(s).getX(item)
  override def getY(s: Int, item: Int) = series(s).getY(item)
  override def getYValue(s: Int, item: Int) = series(s).getY(item)
}
