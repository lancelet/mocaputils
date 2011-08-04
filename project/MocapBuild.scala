import sbt._
import Keys._

object BuildSettings {
  val buildOrganization = "com.github.mocaputils"
  val buildScalaVersion = "2.9.1.RC1"
  val buildVersion      = "0.2-SNAPSHOT"
  val buildSettings = Defaults.defaultSettings ++ Seq(
    organization := buildOrganization,
    scalaVersion := buildScalaVersion,
    version      := buildVersion
  )
}

object Resolvers {
  val scalaToolsSnapshots = "Scala-Tools Snapshots" at
    "http://scala-tools.org/repo-snapshots"
  val ondex = "Ondex" at
    "http://ondex.rothamsted.bbsrc.ac.uk/nexus/content/groups/public"
  val allResolvers = Seq(scalaToolsSnapshots, ondex)
}

object Dependencies {
  val jCommon     = "jfree" % "jcommon" % "1.0.16"
  val jFreeChart  = "jfree" % "jfreechart" % "1.0.13"
  val xmlGraphics = "org.apache.xmlgraphics" % "xmlgraphics-commons" % "1.3.1"
  val iText       = "com.lowagie" % "itext" % "2.1.5" intransitive()
  val scalaz      = "org.scalaz" %% "scalaz-core" % "6.0.2-SNAPSHOT"
  val scalaTest   = "org.scalatest" % "scalatest_2.9.0" % "1.6.1" % "test"
  val scalaCheck  = "org.scala-tools.testing" % "scalacheck_2.9.0" % "1.9" % 
                      "test"
  val scalala     = "org.scalala" % "scalala_2.9.0" % "1.0.0.RC2-SNAPSHOT"
  val allDependencies = Seq(
    jCommon, jFreeChart, xmlGraphics, iText, scalaz, scalaTest, scalaCheck, 
    scalala
  )
}

object MocapBuild extends Build {
  import Resolvers._
  import Dependencies._
  import BuildSettings._

  lazy val mocaputils = Project("mocaputils", file("."), 
				settings = buildSettings ++ Seq(
				  libraryDependencies := allDependencies,
				  resolvers := allResolvers
				)) dependsOn(scalaSignal)
	// can add scalacOptions := Seq("-unchecked") to settings above
  val scalaSignalUri = uri("git://github.com/lancelet/scalasignal.git")
  lazy val scalaSignal = RootProject(scalaSignalUri)
}
