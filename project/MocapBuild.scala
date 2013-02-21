import sbt._
import Keys._

object BuildSettings {
  val buildOrganization = "com.github.mocaputils"
  val buildScalaVersion = "2.10.0"
  val buildVersion      = "0.3-SNAPSHOT"
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
  val xmlGraphics = "org.apache.xmlgraphics" % "xmlgraphics-commons" % "1.5"
  val iText       = "com.lowagie" % "itext" % "4.2.0" intransitive()
  val scalaz      = "org.scalaz" %% "scalaz-core" % "7.0.0-M7"
  val scalaTest   = "org.scalatest" %% "scalatest" % "1.9.1" % "test"
  val scalaCheck  = "org.scalacheck" %% "scalacheck" % "1.10.0" % "test"
  val breeze      = "org.scalanlp" %% "breeze-math" % "0.2-SNAPSHOT"
  val allDependencies = Seq(
    jCommon, jFreeChart, xmlGraphics, iText, scalaz, scalaTest, scalaCheck, 
    breeze
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
