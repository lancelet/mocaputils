package mocaputils.plotting

import scala.collection.immutable._
import org.jfree.data.general.{ SeriesDataset => JFreeSeriesDataset }

/** Wrapper for `org.jfree.data.general.SeriesDataset`.
 * 
 *  Provides an `org.jfree.data.general.SeriesDataset` implementation from
 *  a `Seq[Series]`. */
trait SeriesDataset extends JFreeSeriesDataset {
  val series: Seq[Series]
  override def getSeriesCount() = series.length
  override def getSeriesKey(s: Int) = series(s).name
  override def indexOf(key: Comparable[_]) = series.indexWhere(_.name == key)
}
