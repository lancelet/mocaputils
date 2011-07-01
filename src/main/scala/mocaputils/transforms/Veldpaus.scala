package mocaputils.transforms

import scala.collection.immutable.Seq
import scala.collection.immutable.Traversable
import scala.math.sqrt

import scalala.library.LinearAlgebra.cross
import scalala.library.LinearAlgebra.det
import scalala.library.LinearAlgebra.inv
import scalala.library.LinearAlgebra.rank
import scalala.tensor.dense.DenseMatrix
import scalala.tensor.dense.DenseVectorCol
import scalala.tensor.{:: => $colon$colon}
import scalala.tensor.VectorCol
import scalala.tensor.Matrix

/** Result from the Veldpaus method of computing a rigid body transformation.
 * 
 *  The transformation is: `y = s*[R]*x + v`.
 * 
 *  @param R rotation matrix
 *  @param v translation vector
 *  @param s scale factor
 *  @param xbar centroid of un-transformed set of coordinates
 *  @param ybar centroid of transformed set of coordinates */
case class VeldpausResult(
  R: Matrix[Double],
  v: VectorCol[Double],
  s: Double,
  xbar: VectorCol[Double],
  ybar: VectorCol[Double]
) extends XForm {
  require(R.numCols == 3 && R.numRows == 3)
  require(v.size == 3)
  require(xbar.size == 3)
  require(ybar.size == 3)
  def apply(x: (Double, Double, Double)): (Double, Double, Double) = {
    val xvec = DenseVectorCol(x._1, x._2, x._3)
    val yvec = R * s * xvec + v
    (yvec(0), yvec(1), yvec(2))
  }
}

/** Any exception that may occur (but usually will not) during the
 *  Veldpaus method of rigid body transformation calculation. */
case class VeldpausException(msg: String) extends Exception(msg)

object Veldpaus {

  /** Computes a rigid body transformation from corresponding marker
   *  coordinates.
   *  
   *  The rigid body transformation between two sets of corresponding points is
   *  calculated in a least-squares fashion.  The transformation is given in
   *  the form:
   *  {{{
   *    y = s * R * x + v
   *  }}}
   *  where `x` are the original coordinates and `y` are the transformed
   *  coordinates, `R` is a rotation matrix and `v` is a translation vector.
   *  
   *  The algorithm implemented by this method is described in the paper:
   *  
   *  - Veldpaus FE, Woltring HJ, and Dortmans LJMG (1988) A least-squares
   *      algorithm for the equiform transformation from spatial marker
   *      coordinates.  J Biomech 21(1):45-54.
   *      
   *  @param points a traversable collection of corresponding points
   *  @param tol tolerance for convergence of an iterative part of the
   *    algorithm.  The default value of 1.0e-10 is taken from
   *    Veldpaus et al. (1988). */
  def veldpaus(
    points: Traversable[((Double, Double, Double), (Double, Double, Double))],
    tol: Double = 1.0e-10): VeldpausResult =
  {
    // find x,y coordinates, centroids, and positions relative to centroids
    val (xc, yc) = points.toList.unzip
    val x = xc.map(v => DenseVectorCol(v._1, v._2, v._3))
    val y = yc.map(v => DenseVectorCol(v._1, v._2, v._3))
    def meanv(q: Seq[DenseVectorCol[Double]]): DenseVectorCol[Double] =
      q.reduceLeft(_ + _) / q.length.toDouble
    val (xbar, ybar) = (meanv(x), meanv(y))
    val (xprime, yprime) = (x.map(_ - xbar), y.map(_ - ybar))
    
    // calculate cross-dispersion matrix G, GTG and the adjoint of GTG
    def meanm(q: Seq[DenseMatrix[Double]]): DenseMatrix[Double] =
      q.reduceLeft(_ + _) / q.length.toDouble
    val G = meanm(for ((xp, yp) <- xprime zip yprime) yield yp * xp.t)
    val Ga = adjoint3(G)
    val GTG = G.t * G
    val GTGa = adjoint3(GTG)
    
    // check rank of G; G must be at least rank 2 in order to find a solution
    if (rank(G) < 2) {
      throw VeldpausException("Rank of cross-dispersion matrix was < 2; " +
        "no rigid-body solution is possible.")
    }
    
    // find g1, g2, g3 values; these are invariants of GTG and G, used for
    //  calculating the rotation matrix
    val g1sq = GTG.trace
    val g2sq = GTGa.trace
    val g1 = sqrt(g1sq)
    val g2 = sqrt(g2sq)
    val g3 = det(G)
    
    // compute matrix invariants beta1 and beta2; iterating non-linear
    //  equations using the Newton-Raphson method
    val (beta1, beta2) = {
      val h1 = g2 / g1sq       // sqrt(GTGa.trace) / GTG.trace
      val h2 = g1 * g3 / g2sq  // sqrt(GTG.trace) * det(G) / GTGa.trace
      var xx: Double = 1
      var yy: Double = 1
      var dx: Double = 0
      var dy: Double = 0
      var err = 10.0 * tol
      while (err > tol) {
        val d = xx * yy - h1 * h2
        val help1 = 0.5 * (1.0 - xx * xx + 2.0 * h1 * yy)
        val help2 = 0.5 * (1.0 - yy * yy + 2.0 * h2 * xx)
        val dx = (yy * help1 + h1 * help2) / d
        val dy = (h2 * help1 + xx * help2) / d
        xx = xx + dx
        yy = yy + dy
        err = dx * dx / (xx * xx) + dy * dy / (yy * yy)
      }
      (g1 * xx, g2 * yy)
    }
    
    // calculate R and v
    val R = (G * beta1 + Ga) * inv(GTG + DenseMatrix.eye[Double](3) * beta2)
    val v = ybar - R * xbar
    
    // calculate scale factor s
    val A = meanm(xprime.map(xp => xp * xp.t))
    val B = R.t * G
    val s = B.trace / A.trace
    
    // Veldpaus result
    VeldpausResult(R, v, s, xbar, ybar)
  }
  
  /** Adjoint of a 3x3 matrix.
   * 
   *  @param m matrix for which to compute the adjoint
   *  @return adjoint matrix */
  private def adjoint3(m: Matrix[Double]): DenseMatrix[Double] = {
    require(m.numCols == 3 && m.numRows == 3)
    val c1 = new DenseVectorCol(m(::, 0).toArray)
    val c2 = new DenseVectorCol(m(::, 1).toArray)
    val c3 = new DenseVectorCol(m(::, 2).toArray)
    val at = DenseMatrix(
      cross(c2, c3).toArray,
      cross(c3, c1).toArray,
      cross(c1, c2).toArray)
    DenseMatrix.horzcat(at.t)
  }
}