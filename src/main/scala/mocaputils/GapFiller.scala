package mocaputils

import scala.collection.immutable._

object GapFiller {

  /** Fill gaps in marker data via linear interpolation. */
  def fillGapsLerp(m: GappedMarker, maxSize: Option[Int] = None): 
  Option[Marker] = {
    // check that the marker has some coordinates (it must exist)
    assert(m.exists, "Marker \"%s\" has no coordinates at all!" format m.name)
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
    val coVector = Vector.empty[Vec3] ++
      new IndexedSeq[Vec3] {
        private def inGap(idx: Int): Boolean = {
          m.gaps.exists(x => x._1 <= idx && x._2 > idx)
        }
        private def lerp(idx: Int): Vec3 = {
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
          val x = a.x + f * (b.x - a.x)
          val y = a.y + f * (b.y - a.y)
          val z = a.z + f * (b.z - a.z)
          Vec3(x, y, z)
        }
        val length = m.co.length
        def apply(idx: Int) = if (inGap(idx)) lerp(idx) else m.co(idx).get         
      }

    // new marker
    Some(Marker.Vec3Marker(m.name, m.fs, coVector))
  }

}
