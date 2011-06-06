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
  
  // a marker with a discontinuity
  private val mWithDiscont = new Marker {
    val name = "Test-WithDiscont"
    val co = Vector[(Double, Double, Double)](
      (0,0,0), (0,0,0), (0,0,0), (0,0,0),
      (5,5,5), (10,10,10), (20,20,20),
      (30,30,30), (30,30,30), (30,30,30), (30.1, 30.1, 30.1)
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
  
  test("discontinuities") {
    val expected1 = Seq[(Int, Int)]((3, 7))
    mWithDiscont.discontinuities(1.0) should be (expected1)
    val expected2 = Seq[(Int, Int)]((3, 7), (9, 10))
    mWithDiscont.discontinuities(0.16) should be (expected2)
  }
  
  test("bandwidth") (pending)
  
  test("butter2") (pending)
  
}