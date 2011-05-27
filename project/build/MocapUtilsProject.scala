import sbt._
import de.element34.sbteclipsify._

class MocapUtilsProject(info: ProjectInfo) extends DefaultProject(info) 
with Eclipsify {

  val scalaToolsSnapshots = "Scala Tools Snapshots" at 
    "http://scala-tools.org/repo-snapshots"
  val scalaz = "org.scalaz" %% "scalaz-core" % "6.0-SNAPSHOT"

  val scalaTest = "org.scalatest" %% "scalatest" % "1.4.1"
  // val scalaCheck = "org.scala-tools.testing" %% "scalacheck" % "1.8"

  // other repos
  val scalaNLPRepo = "ScalaNLP" at "http://repo.scalanlp.org/repo"
  val ondexRepo = "ondex" at 
    "http://ondex.rothamsted.bbsrc.ac.uk/nexus/content/groups/public"
  
  // JFreeChart
  val JCommon = "jfree" % "jcommon" % "1.0.16"
  val JFreeChart = "jfree" % "jfreechart" % "1.0.13"
  val XMLGraphicsCommons = "org.apache.xmlgraphics" % "xmlgraphics-commons" % 
      "1.3.1"
  val IText = "com.lowagie" % "itext" % "2.1.5" intransitive()    
  
  // scalasignal
  val scalaSignal = "GitHub" %% "scalasignal" % "0.3-SNAPSHOT"
  
  // enable unchecked warnings
  //override def compileOptions = super.compileOptions ++ Seq(Unchecked)

}
