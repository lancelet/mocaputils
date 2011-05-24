package mocaputils

import collection.immutable._
import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers

class MarkerTest extends FunSuite with ShouldMatchers {

  private val m = new Marker {
    val name = "Test"
    val co = Vector[(Double, Double, Double)](
      (1,2,3), (4,5,6), (7,8,9)
    )
    val fs = 100.0
  }
  
  test("check that xs correctly fetches x coordinates") {
    val expected = Vector[Double](1, 4, 7)
    m.xs should be (expected)
  }
  
  test("check that ys correctly fetches y coordinates") {
    val expected = Vector[Double](2, 5, 8)
    m.ys should be (expected)
  }
  
  test("check that zs correctly fetches z coordinates") {
    val expected = Vector[Double](3, 6, 9)
    m.zs should be (expected)
  }
  
  test("bandwidth") (pending)
  
}