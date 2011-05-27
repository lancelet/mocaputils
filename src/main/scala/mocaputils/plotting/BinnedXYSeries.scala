package mocaputils.plotting

/** Extension of `XYSeries` providing bin sizes (eg. for histograms). 
 * 
 *  This trait is intended to be used in combination with 
 *  `XYBinnedDataset`. */
trait BinnedXYSeries extends XYSeries {
  def getStartX(item: Int): Double = 0.0
  def getEndX(item: Int): Double = 0.0
  def getStartY(item: Int): Double = 0.0
  def getEndY(item: Int): Double = 0.0
}