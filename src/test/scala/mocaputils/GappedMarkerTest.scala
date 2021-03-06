package mocaputils

import collection.immutable._
import org.scalatest.FunSuite

class GappedMarkerTest extends FunSuite {

  private val m = new GappedMarker.GappedMarkerLike 
  with GappedMarker.XYZFromCo {
    val name = "Test"
    val co = Vector[Option[Vec3]](
      None, None, Some(Vec3(1,2,3)), Some(Vec3(4,5,6)), Some(Vec3(7,8,9)), None
    )
    val fs = 100.0
  }
  
  test("check that xs correctly fetches x coordinates") {
    val expected = Vector[Option[Double]](
        None, None, Some(1), Some(4), Some(7), None)
    assert(m.xs === expected)
  }
  
  test("check that ys correctly fetches y coordinates") {
    val expected = Vector[Option[Double]](
        None, None, Some(2), Some(5), Some(8), None)
    assert(m.ys === expected)
  }
  
  test("check that zs correctly fetches z coordinates") {
    val expected = Vector[Option[Double]](
        None, None, Some(3), Some(6), Some(9), None)
    assert(m.zs === expected)
  }
  
  test("check that range is correctly evaluated") {
    assert(m.range === Tuple2(2,4))
  }
  
  test("check that gaps are correctly found") {
    assert(m.gaps === List((0, 2), (5, 6)))
  }
  
  test("check the exists flag") {
    assert(m.exists === true)
    val mNoCoords = new GappedMarker.GappedMarkerLike 
    with GappedMarker.XYZFromCo {
      val name = "NotPresent"
      val co = Vector[Option[Vec3]](None, None, None)
      val fs = 100.0
    }
    assert(mNoCoords.exists === false)
  }
  
  test("check that slice works") {
    val expected = Vector[Option[Vec3]](
      Some(Vec3(1,2,3)), Some(Vec3(4,5,6)), Some(Vec3(7,8,9)), None
    )
    assert(m.slice(2, 6).co === expected)
  }
  
}