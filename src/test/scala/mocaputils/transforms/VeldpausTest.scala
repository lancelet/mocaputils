package mocaputils.transforms

import scala.collection.immutable.List
import scala.collection.immutable.Seq
import scala.math.abs
import scala.math.cos
import scala.math.max
import scala.math.sin

import org.scalacheck.Arbitrary
import org.scalacheck.Gen
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.prop.Checkers
import org.scalatest.FunSuite

import scalala.library.Library.norm
import scalala.library.LinearAlgebra.det
import scalala.tensor.dense.DenseMatrix
import scalala.tensor.dense.DenseVector
import scalala.tensor.dense.DenseVectorCol
import scalala.tensor.Tensor

import mocaputils.Vec3

class VeldpausTest extends FunSuite with Checkers with ShouldMatchers {

  private val eps = 1.0e-10

  /** Takes absolute value of all elements of a tensor and returns the largest. 
   */
  private def absmax(m: Tensor[_, Double]): Double = 
    max(abs(m.max), abs(m.min))
  
  test("veldpaus specific") {
    // test an exact 2D transformation, which has been calculated manually
    // the transformation is:
    //  1. Rotate -90 deg about z.
    //  2. Translate by (-1,1,0)
    val x = Seq[Vec3](Vec3(1,1,0), Vec3(2, 1,0), Vec3(1,2,0))
    val y = Seq[Vec3](Vec3(0,0,0), Vec3(0,-1,0), Vec3(1,0,0))
    val vr = Veldpaus.veldpaus(x zip y)
    
    val Rexpected = DenseMatrix(
      Array[Double]( 0, 1, 0),
      Array[Double](-1, 0, 0),
      Array[Double]( 0, 0, 1))
    val vexpected = DenseVectorCol(-1.0, 1.0, 0.0)
    val xbarexpected = DenseVectorCol(1 + 1.0/3.0, 1 + 1.0/3.0, 0.0)
    val ybarexpected = DenseVectorCol(1.0/3.0, -1.0/3.0, 0.0)
    absmax(vr.R - Rexpected) should be < (eps)
    absmax(vr.v - vexpected) should be < (eps)
    vr.s should be (1.0 plusOrMinus eps)
    absmax(vr.xbar - xbarexpected) should be < (eps)
    absmax(vr.ybar - ybarexpected) should be < (eps)
  }

  // arbitrary offset vectors and rotation matrices for ScalaCheck / ScalaTest
  case class OffsetVector(v: DenseVectorCol[Double]) { assert(v.length == 3) }
  case class RotationMatrix(m: DenseMatrix[Double]) {
    assert(m.numRows == 3 && m.numCols == 3)
    require(abs(det(m) - 1.0) < 1.0e-5) // det(m) == 1
  }
  implicit def arbOffsetVector: Arbitrary[OffsetVector] = Arbitrary {
    for {
      vs <- Gen.containerOfN[Array, Double](3,
        Gen.chooseNum[Double](-100.0, 100.0))
      if (vs.sum != 0.0)
    } yield OffsetVector(DenseVectorCol(vs(0), vs(1), vs(2)))
  }
  implicit def arbRotationMatrix: Arbitrary[RotationMatrix] = Arbitrary {
    for {
      axis  <- Arbitrary.arbitrary[OffsetVector]
      alpha <- Arbitrary.arbitrary[Double]
      nAxis = axis.v / norm(axis.v, 2)
    } yield {
      // create random rotation matrix from axis-angle representation
      val a = cos(alpha / 2)
      val s = sin(alpha / 2)
      val b = nAxis(0) * s
      val c = nAxis(1) * s
      val d = nAxis(2) * s
      // quat -> rotation matrix
      RotationMatrix(DenseMatrix(
        Array(a*a+b*b-c*c-d*d, 2.0*b*c-2.0*a*d, 2.0*b*d+2.0*a*c),
        Array(2.0*b*c+2.0*a*d, a*a-b*b+c*c-d*d, 2.0*c*d-2.0*a*b),
        Array(2.0*b*d-2.0*a*c, 2.0*c*d+2.0*a*b, a*a-b*b-c*c+d*d)))        
    }
  }
  
  // simulated marker cluster
  val cluster = Seq(
    DenseVectorCol( 0.0,  0.0, 0.0),
    DenseVectorCol(10.0,  0.0, 2.0),
    DenseVectorCol(-5.0,  4.0, 3.0),
    DenseVectorCol(-8.0, -2.0, 2.0))
  
  test("veldpaus random") {
    // check that for random, but not noisy transforms, veldpaus works
    check((R: RotationMatrix, v: OffsetVector) => {
      def vecToVec(vec: DenseVector[Double]) = Vec3(vec(0), vec(1), vec(2))
      val ys = cluster.map(R.m * _ + v.v)  // transform marker cluster
      val vr = Veldpaus.veldpaus(
        cluster.map(vecToVec) zip ys.map(vecToVec))
      // check rotation matrix
      val rotCheck = absmax(vr.R - R.m) < 1e-4
      // check offset vector
      val vectorCheck = norm(vr.v - v.v, 2) < 1e-4
      // check scale
      val scaleCheck = abs(vr.s - 1) < 1e-4
      
      rotCheck && vectorCheck && scaleCheck
    })
  }
  
  test("veldpaus noisy and random") {
    val noiseAmt = 0.2
    // check that for random transforms, with noise added to the markers,
    //  we still get approximately the correct result
    check((R: RotationMatrix, v: OffsetVector) => {
      def vecToVec(vec: DenseVector[Double]) = Vec3(vec(0), vec(1), vec(2))
      val ys = cluster.map(R.m * _ + v.v)  // gold standard y positions
      // add random noise to the y positions
      val randomVecs = List.fill[DenseVectorCol[Double]](ys.length) {
        def r: Double = scala.util.Random.nextGaussian
        DenseVectorCol(r, r, r) * noiseAmt
      }
      val ysNoisy = for ((y, r) <- ys zip randomVecs) yield (y + r)
      
      // compute the transformation
      val vr = Veldpaus.veldpaus(
        cluster.map(vecToVec) zip ys.map(vecToVec))
      
      // map the cluster to their least-squares ys positions
      val ysLsq = cluster.map(vecToVec).map(vr).map(t =>
        DenseVectorCol(t.x, t.y, t.z))
        
      // test errors
      val errors = for ((y, yl) <- ys zip ysLsq) yield norm(y - yl, 2)
      val errorTest = errors.forall(_ >= 0.0) && (errors.max < 10.0*noiseAmt)
    
      // test scale
      val scaleTest = abs(vr.s - 1) < noiseAmt
      
      // determinant test: the determinant of the rotation matrix must be 1
      val detTest = abs(det(vr.R) - 1) < 1e-6
      
      // confirm all tests
      errorTest && scaleTest && detTest
    })
  }
  
}
