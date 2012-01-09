import sbt._
import Keys._

object BuildSettings {
  val buildOrganization = "com.github.mocaputils"
  val buildScalaVersion = "2.9.1"
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
  val jCommon     = "org.jfree" % "jcommon" % "1.0.17"
  val jFreeChart  = "org.jfree" % "jfreechart" % "1.0.14"
  val xmlGraphics = "org.apache.xmlgraphics" % "xmlgraphics-commons" % "1.3.1"
  val iText       = "com.lowagie" % "itext" % "2.1.5" intransitive()
  val scalaz      = "org.scalaz" %% "scalaz-core" % "7.0-SNAPSHOT"
  val scalaTest   = "org.scalatest" %% "scalatest" % "1.6.1" % "test"
  val scalaCheck  = "org.scala-tools.testing" %% "scalacheck" % "1.9" % 
                      "test"
  val scalala     = "org.scalala" %% "scalala" % "1.0.0.RC2-SNAPSHOT"
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
