import sbt._
import Keys._

object BuildSettings {
  val buildOrganization = "com.github.mocaputils"
  val buildScalaVersion = "2.9.2"
  val buildVersion      = "0.2-SNAPSHOT"
  val buildSettings = Defaults.defaultSettings ++ Seq(
    organization := buildOrganization,
    scalaVersion := buildScalaVersion,
    version      := buildVersion
  )
}

object Resolvers {
  val sonatype = "Sonatype" at "https://oss.sonatype.org/content/groups/public"
  val mvnrepo = "MvnRepository" at "http://mvnrepository.com/"
  val allResolvers = Seq(sonatype, mvnrepo)
}

object Dependencies {
  val jCommon     = "org.jfree" % "jcommon" % "1.0.17"
  val jFreeChart  = "org.jfree" % "jfreechart" % "1.0.14"
  val xmlGraphics = "org.apache.xmlgraphics" % "xmlgraphics-commons" % "1.4"
  val iText       = "com.lowagie" % "itext" % "2.1.5" intransitive()
  val scalaz      = "org.scalaz" %% "scalaz-core" % "7.0-SNAPSHOT"
  val scalaTest   = "org.scalatest" %% "scalatest" % "1.7.2" % "test"
  val scalaCheck  = "org.scalacheck" %% "scalacheck" % "1.9" % "test"
  val scalala     = "org.scalala" % "scalala_2.9.1" % "1.0.0.RC2"
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
