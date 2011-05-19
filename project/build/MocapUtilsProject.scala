import sbt._

class MocapUtilsProject(info: ProjectInfo) extends DefaultProject(info) {

  val scalaToolsSnapshots = "Scala Tools Snapshots" at "http://scala-tools.org/repo-snapshots"
  val scalaz = "org.scalaz" %% "scalaz-core" % "6.0-SNAPSHOT"

  val scalaTest = "org.scalatest" %% "scalatest" % "1.4.1"
  // val scalaCheck = "org.scala-tools.testing" %% "scalacheck" % "1.8"

  // enable unchecked warnings
  //override def compileOptions = super.compileOptions ++ Seq(Unchecked)

}
