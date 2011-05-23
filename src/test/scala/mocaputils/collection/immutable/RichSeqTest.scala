package mocaputils.collection.immutable

import org.scalatest.FunSuite

class RichSeqTest extends FunSuite {

  test("slicesWhere test") {
    // random
    val s1 = List[Option[Int]](
      None, Some(1), Some(2), Some(3), None, None, Some(4), Some(5),
      None, Some(6), Some(7), Some(8), Some(9), None
    )
    val r1 = RichSeq(s1).slicesWhere(_.isDefined)
    assert(r1 === List((1, 4), (6, 8), (9, 13)))
    
    // no slices
    val s2 = List[Option[Int]](None, None)
    val r2 = RichSeq(s2).slicesWhere(_.isDefined)
    assert(r2 === List[(Int, Int)]())
    
    // all in one slice
    val s3 = List[Option[Int]](Some(1), Some(2), Some(3))
    val r3 = RichSeq(s3).slicesWhere(_.isDefined)
    assert(r3 === List((0, 3)))
  }
  
}