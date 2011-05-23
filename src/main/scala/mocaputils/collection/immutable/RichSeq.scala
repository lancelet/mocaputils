package mocaputils.collection.immutable

import annotation.tailrec
import scala.collection.immutable._
import scala.collection.mutable.ListBuffer

/** A rich sequence type which defines some new, useful methods. */
class RichSeq[+A](val self: Seq[A]) {

  /** Returns a sequences of slices that define a region in which the specified 
   *  predicate is contiguously true.
   *  
   *  In the sequence of slices, the first part of each tuple specifies the
   *  first index of the slice to include, while the second part specifies the
   *  first index of the slice to EXCLUDE.  This behaviour corresponds with
   *  that of the `slice` method of sequences. 
   *  
   *  @param p predicate which must be true contiguously within the slices
   *  @param from first index from which to search from contiguous regions
   *  @return sequence of slices in which the predicate is true */
  final def slicesWhere(p: (A) => Boolean, from: Int): List[(Int, Int)] = {
    val buf = new ListBuffer[(Int, Int)]
    var si = from
    var ei = si - 1
    while (si != -1 && ei < self.length) {
      si = self.indexWhere(p, ei + 1)
      if (si != -1) {
        ei = si
        while (ei < self.length && p(self(ei)) == true) { ei = ei + 1 }
        buf += ((si, ei))
      }
    }
    buf.toList
  }
  
  /** Returns a sequences of slices that define a region in which the specified 
   *  predicate is contiguously true.
   *  
   *  In the sequence of slices, the first part of each tuple specifies the
   *  first index of the slice to include, while the second part specifies the
   *  first index of the slice to EXCLUDE.  This behaviour corresponds with
   *  that of the `slice` method of sequences. 
   *  
   *  @param p predicate which must be true contiguously within the slices
   *  @return sequence of slices in which the predicate is true */
  def slicesWhere(p: (A) => Boolean): List[(Int, Int)] = slicesWhere(p, 0)
  
}

object RichSeq {
  /** Creates a new instance of RichSeq. */
  def apply[A](self: Seq[A]): RichSeq[A] = new RichSeq(self)
}
