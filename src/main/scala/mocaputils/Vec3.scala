package mocaputils

trait Vec3 {
  def x: Double
  def y: Double
  def z: Double
}

object Vec3 {
  def apply(x: Double, y: Double, z: Double): Vec3 = SimpleVec3(x, y, z)
  
  private final case class SimpleVec3(
      x: Double, 
      y: Double, 
      z: Double
      ) extends Vec3
}