package mocaputils

import collection.immutable._
import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers
import Comparisons._

class MarkerTest extends FunSuite with ShouldMatchers {

  private val m = new Marker with Marker.XYZFromCo {
    val name = "Test"
    val co = Vector[Vec3](
      Vec3(1,2,3), Vec3(4,5,6), Vec3(7,8,9)
    )
    val fs = 100.0
  }
  
  // a marker with a discontinuity
  private val mWithDiscont = new Marker with Marker.XYZFromCo {
    val name = "Test-WithDiscont"
    val co = Vector[Vec3](
      Vec3(0,0,0), Vec3(0,0,0), Vec3(0,0,0), Vec3(0,0,0),
      Vec3(5,5,5), Vec3(10,10,10), Vec3(20,20,20),
      Vec3(30,30,30), Vec3(30,30,30), Vec3(30,30,30), Vec3(30.1, 30.1, 30.1)
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
  
  test("butter2") {
    val marker = new Marker with Marker.XYZFromCo {
      val name = "Test"
      val fs = 10.0
      val co = Vector[Vec3] (
        Vec3(0,8,1), Vec3(1,-3,1), Vec3(6,0,0), Vec3(2,2,-1), Vec3(3,5,2), 
        Vec3(1,8,0), Vec3(6,7,1)
      )
    }
    val xExpected = Vector(-0.10493, 1.07792, 2.09518, 2.93073, 3.77711, 
                            4.86018, 6.18174)
    val yExpected = Vector(7.9450, 5.2331, 3.4951, 3.1033, 3.7941, 5.0689, 
                           6.5573)
    val zExpected = Vector(0.98498, 0.68859, 0.47804, 0.41952, 0.51461, 
                           0.71039, 0.95353)
    val filtMarker = marker.butter2(1.0)
    eqd(filtMarker.xs, xExpected, 1e-5)
    eqd(filtMarker.ys, yExpected, 1e-4)
    eqd(filtMarker.zs, zExpected, 1e-5)
  }
  
}