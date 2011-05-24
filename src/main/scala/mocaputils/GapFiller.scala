package mocaputils

import scala.collection.immutable._

object GapFiller {

  /** Fill gaps in marker data via linear interpolation. */
  def fillGapsLerp(m: GappedMarker, maxSize: Option[Int] = None): 
  Option[Marker] = { 
    // check the maximum gap size against what we're allowed to fill
    if (maxSize.isDefined) {
      if (m.gaps.map(x => x._2 - x._1).max > maxSize.get) {
        return None
      }
    }
    
    // create a coordinate vector, filling gaps on the fly
    //  TODO: increase efficiency here, if it turns out to be a bottle-neck.
    //        some re-evaluations happen often, where they only need to happen
    //        once
    val coVector = Vector.empty[(Double, Double, Double)] ++
      new IndexedSeq[(Double, Double, Double)] {
        private def inGap(idx: Int): Boolean = {
          m.gaps.exists(x => x._1 <= idx && x._2 > idx)
        }
        private def lerp(idx: Int): (Double, Double, Double) = {
          val gap = m.gaps.find(x => x._1 <= idx && x._2 > idx).get
          val g0 = if (gap._1 == 0) gap._2 else (gap._1 - 1)
          val g1 = if (gap._2 == m.co.length) (gap._1 - 1) else gap._2
          assert(g0 >= 0 && g0 < m.co.length)
          assert(g1 >= 0 && g1 < m.co.length)
          assert(g1 >= g0)
          assert(!inGap(g0))
          assert(!inGap(g1))
          val a = m.co(g0).get
          val b = m.co(g1).get
          val f = (idx - gap._1 + 1).toDouble / (gap._2 - gap._1 + 1).toDouble
          val x = a._1 + f * (b._1 - a._1)
          val y = a._2 + f * (b._2 - a._2)
          val z = a._3 + f * (b._3 - a._3)
          (x, y, z)
        }
        val length = m.co.length
        def apply(idx: Int) = if (inGap(idx)) lerp(idx) else m.co(idx).get         
      }

    // new marker
    Some(new Marker {
      val name = m.name
      val co = coVector
    })
  }
  
}
