package mocaputils

import io.Source
import org.scalatest.FunSuite

class TRCReaderTest extends FunSuite {

  test("read - invalid file name") {
    val res = TRCReader.read("fubar.trc")
    res.fold(
      e => {
        /* error expected */
      },
      s => fail()
    )
  }

  test("read - test file") {
    val inStream = getClass.getResourceAsStream("testFile.trc")
    val it = Source.fromInputStream(inStream).getLines
    val res = TRCReader.read(it)
    res.fold(
      e => fail(),
      s => {
        assert(s.internalFileName === "G:\\MyTest\\TestFile.trc")
        assert(s.dataRate === 60.0)
        assert(s.cameraRate === 60.0)
        assert(s.numFrames === 5)
        assert(s.numMarkers === 9)
        assert(s.units === "mm")
        assert(s.origDataRate === 60.0)
        assert(s.origDataStartFrame === 1)
        assert(s.origNumFrames === 2690)

        val markerNames = s.markers.map(_.name).sorted
        val expNames = List("Head", "Rein1", "RFHoofL", "LFHoofL", "Surcingle",
          "S2", "RHHoofL", "LHHoofL", "Rein2").sorted
        assert(markerNames === expNames)

        // check head marker
        val expCoords = Vector(
          Vec3(1247.12988, 649.67725, 1446.53552),
          Vec3(1247.26807, 650.12024, 1447.11145),
          Vec3(1246.90698, 650.64636, 1447.61157),
          Vec3(1246.85059, 650.76019, 1447.70544),
          Vec3(1246.60803, 651.06555, 1447.99841)
        ).map(Some(_))
        assert(s.getMarker("Head").co === expCoords.toList)
        assert(s.getMarker("Head").range === Tuple2(0, 4))
        assert(s.getMarker("Head").fs === 60.0)
      }
    )
  }

}
