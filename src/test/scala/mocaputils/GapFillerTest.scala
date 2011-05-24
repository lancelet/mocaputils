package mocaputils

import collection.immutable._
import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers

class GapFillerTest extends FunSuite with ShouldMatchers {
  
  test("test linear gap filling - ends empty") {
    val mGap = new GappedMarker {
      val name = "Test"
      val co = Vector[Option[(Double, Double, Double)]](
        None, None, Some(1, 2, 3), Some(4, 5, 6), None, Some(8, 9, 10), None
      )
    }
    val m = GapFiller.fillGapsLerp(mGap).get
    val xs = Vector[Double](1, 1, 1, 4, 6, 8, 8)
    val ys = Vector[Double](2, 2, 2, 5, 7, 9, 9)
    val zs = Vector[Double](3, 3, 3, 6, 8, 10, 10)
    m.xs should be (xs)
    m.ys should be (ys)
    m.zs should be (zs)
  }
  
  test("test linear gap filling - multiple elements") {
    val mGap = new GappedMarker {
      val name = "Test"
      val co = Vector[Option[(Double, Double, Double)]](
        Some(1,1,1), None, None, None, None, Some(6,6,6)
      )
    }
    val m = GapFiller.fillGapsLerp(mGap).get
    val ss = Vector[Double](1, 2, 3, 4, 5, 6)
    m.xs should be (ss)
    m.ys should be (ss)
    m.zs should be (ss)
  }
  
}
