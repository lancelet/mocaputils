package mocaputils.plotting

import org.jfree.data.general.DatasetChangeListener
import org.jfree.data.general.Dataset
import org.jfree.data.general.DatasetGroup

/** Static Dataset. */
trait StaticDataset extends Dataset {
  override def addChangeListener(l: DatasetChangeListener) {}
  override def removeChangeListener(l: DatasetChangeListener) {}
  private var _datasetGroup: DatasetGroup = null
  override def setGroup(g: DatasetGroup) { _datasetGroup = g }
  override def getGroup() = _datasetGroup
}
