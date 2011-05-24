package mocaputils

import scala.collection.immutable._

/** A marker (without any gaps). */
trait Marker {
  /** Name of the marker. */
  val name: String
  /** Marker coordinates. */
  val co: IndexedSeq[(Double, Double, Double)]
  
  /** `x`-values of the marker coordinates */
  lazy val xs: IndexedSeq[Double] = co.map(_._1)
  /** `y`-values of the marker coordinates */
  lazy val ys: IndexedSeq[Double] = co.map(_._2)
  /** `z`-values of the marker coordinates */
  lazy val zs: IndexedSeq[Double] = co.map(_._3)
}