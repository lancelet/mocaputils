package mocaputils

import scala.collection.immutable.Vector
import org.scalatest.FunSuite
import scalaz.{ Success, Failure }

class ReadUtilsTest extends FunSuite {

  test("nextLine") {
    val it = List("Test").iterator
    assert(ReadUtils.nextLine(it) === Success("Test"))
    assert(ReadUtils.nextLine(it).isFailure)
  }

  test("fetchLineAndCheck - valid line") {
    val it = List("Sample Rate:\t400\tName:\tTest").iterator
    val res = ReadUtils.fetchLineAndCheck(it, 
					  Option("Sample Rate:"), None,
					  Option("Name:"), None)
    res.fold(
      e => fail(),
      s => assert(s === List("Sample Rate:", "400", "Name:", "Test"))
    )
  }

  test("fetchLineAndCheck - wrong number of items") {
    val it = List("Sample Rate:\t400\tName:\tTest").iterator
    val res = ReadUtils.fetchLineAndCheck(it, None)
    res.fold(
      e => { /* error is expected */ },
      s => fail()
    )
  }

  test("fetchLineAndCheck - token mismatch") {
    val it = List("Sample Rate:\t400\tName:\tTest").iterator
    val res = ReadUtils.fetchLineAndCheck(it,
					  Option("Sample Rate:"), None,
					  Option("Blah:"), None)
    res.fold(
      e => { /* error is expected */ },
      s => fail()
    )
  }

  test("getDouble - valid item") {
    val v = Vector("Sample Rate:", "400.0")
    val res = ReadUtils.getDouble(v, 1)
    res.fold(
      e => fail(),
      s => assert(s === 400.0)
    )
  }

  test("getDouble - invalid item") {
    val v = Vector("Sample Rate:", "Test")
    val res = ReadUtils.getDouble(v, 1)
    res.fold(
      e => { /* error is expected */ },
      s => fail()
    )
  }

  test("getInt - valid item") {
    val v = Vector("Number of Samples:", "42")
    val res = ReadUtils.getInt(v, 1)
    res.fold(
      e => fail(),
      s => assert(s === 42)
    )
  }

  test("getInt - invalid item") {
    val v = Vector("Number of Samples:", "Test")
    val res = ReadUtils.getInt(v, 1)
    res.fold(
      e => { /* error is expected */ },
      s => fail()
    )
  }

  test("skipLines") {
    val it = List("Line 1", "Line 2", "Line 3", "Line 4", "Line 5").iterator
    val r1 = ReadUtils.skipLines(it, 3)
    assert(r1 === None)
    val nl = ReadUtils.nextLine(it)
    nl.fold(e => fail(), s => assert(s === "Line 4"))
    val r2 = ReadUtils.skipLines(it, 100)
    r2 match {
      case None => fail()
      case s: Some[_] => /* error is expected */
    }
  }

  test("getSingleIntField - valid field") {
    val it = List("NSamples=3").iterator
    ReadUtils.getSingleIntField(it, "NSamples").fold(
      e => fail(),
      s => assert(s === 3)
    )
  }

  test("getSingleIntField - not a field") {
    val it = List("NSamples=3:Bar=42").iterator
    ReadUtils.getSingleIntField(it, "NSamples").fold(
      e => { /* error is expected */ },
      s => fail()
    )
  }

  test("getSingleIntField - incorrect name") {
    val it = List("YSamples=3").iterator
    ReadUtils.getSingleIntField(it, "NSamples").fold(
      e => { /* error is expected */ },
      s => fail()
    )
  }

  test("getSingleDoubleField - valid field") {
    val it = List("SampleRate=960.0").iterator
    ReadUtils.getSingleDoubleField(it, "SampleRate").fold(
      e => fail(),
      s => assert(s === 960.0)
    )
  }

  test("getSingleDoubleField - not a field") {
    val it = List("SampleRate=960.0:Bar=42").iterator
    ReadUtils.getSingleDoubleField(it, "SampleRate").fold(
      e => { /* error is expected */ },
      s => fail()
    )
  }

  test("getSingleDoubleField - incorrect name") {
    val it = List("SampleBar=960.0").iterator
    ReadUtils.getSingleDoubleField(it, "NSamples").fold(
      e => { /* error is expected */ },
      s => fail()
    )
  }

}
