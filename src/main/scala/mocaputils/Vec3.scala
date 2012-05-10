package mocaputils

trait Vec3 {
  def x: Double
  def y: Double
  def z: Double
  
  def +(v: Vec3): Vec3
  def /(d: Double): Vec3
}

object Vec3 {
  def apply(x: Double, y: Double, z: Double): Vec3 = SimpleVec3(x, y, z)
  
  trait Ops { self: Vec3 =>
    def +(v: Vec3): Vec3 = SimpleVec3(x + v.x, y + v.y, z + v.z)
    def /(d: Double): Vec3 = SimpleVec3(x / d, y / d, z / d)
  }
  
  private final case class SimpleVec3(
      x: Double, 
      y: Double, 
      z: Double
      ) extends Vec3 with Ops
}