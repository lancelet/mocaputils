package mocaputils

import io.Source
import org.scalatest.FunSuite

class ForceReaderTest extends FunSuite {

  test("read - invalid file name") {
    ForceReader.read("fubar.forces").fold(
      e => { /* error expected */ },
      s => fail()
    )
  }

  test("read - test file") {
    val inStream = getClass.getResourceAsStream("testFile.forces")
    val it = Source.fromInputStream(inStream).getLines
    ForceReader.read(it).fold(
      e => fail(),
      s => {
	assert(s.nPlates === 2)
	assert(s.sampleRate === 960.0)
	assert(s.numSamples === 3)

	// check force plate 2 (at index 1)
	val expForces = Vector(
	  (-46.88, -3.91, 1966.80),
	  (-47.85, -3.91, 1964.84),
	  (-47.85, -3.91, 1966.80)
	)
	val expCOP = Vector(
	  (-484.30, 386.40, 0.00),
	  (-484.31, 386.18, 0.00),
	  (-484.30, 386.10, 0.00)
	)
	val expMZ = Vector(-16849.78, -16924.04, -16919.89)
	assert(s.getPlate(1).forceIterator.toList === expForces.toList)
	assert(s.getPlate(1).copIterator.toList === expCOP.toList)
	assert(s.getPlate(1).mzIterator.toList === expMZ.toList)
	assert(s.getPlate(1).range._1 === 0)
	assert(s.getPlate(1).range._2 === 2)
      }
    )
  }

}
