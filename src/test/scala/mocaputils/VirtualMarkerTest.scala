package mocaputils

import scala.collection.immutable._
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.FunSuite

class VirtualMarkerTest extends FunSuite with ShouldMatchers {

  private val eps = 1e-10
  
  test("check that virtual markers track their host markers") {

    // three markers which undergo a pre-computed transformation, which has
    //  been calculated manually.  the transformation is:
    //   1. rotate -90 deg about z
    //   2. translate by (-1,1,0)
    val marker1 = new Marker {
      val name = "marker1"
      val fs = 1.0
      val co = IndexedSeq[Vec3](Vec3(1,1,0), Vec3(0,0,0))
    }
    val marker2 = new Marker {
      val name = "marker2"
      val fs = 1.0
      val co = IndexedSeq[Vec3](Vec3(2,1,0), Vec3(0,-1,0))
    }
    val marker3 = new Marker {
      val name = "marker3"
      val fs = 1.0
      val co = IndexedSeq[Vec3](Vec3(1,2,0), Vec3(1,0,0))
    }
    val markers = Seq(marker1, marker2, marker3)
    val firstCo = markers.map(_.co(0))
    
    // construct the virtual marker, using the first frame of the markers
    //  as the reference.  this marker is at (0,0,0)
    val vmarker = new VirtualMarker("virtual", Vec3(0,0,0), firstCo zip markers)
    
    // check the coordinates of the virtual marker at frames 0 and 1
    vmarker.xs(0) should be (0.0 plusOrMinus eps)
    vmarker.ys(0) should be (0.0 plusOrMinus eps)
    vmarker.zs(0) should be (0.0 plusOrMinus eps)
    vmarker.xs(1) should be (-1.0 plusOrMinus eps)
    vmarker.ys(1) should be (1.0 plusOrMinus eps)
    vmarker.zs(1) should be (0.0 plusOrMinus eps)
  }
  
}
