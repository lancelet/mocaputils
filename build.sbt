lazy val mocaputils = project.in(file(".")).settings(
  name := "mocaputils",
  version := "0.3-SNAPSHOT",
  scalaVersion := "2.10.3",
  libraryDependencies ++= List(
    "org.jfree"              %  "jcommon"             % "1.0.17",
    "org.jfree"              %  "jfreechart"          % "1.0.14",
    "org.apache.xmlgraphics" %  "xmlgraphics-commons" % "1.5",
    "com.lowagie"            %  "itext"               % "4.2.0" intransitive(),
    "org.scalaz"             %% "scalaz-core"         % "7.0.5",
    "org.scalatest"          %% "scalatest"           % "1.9.1" % "test",
    "org.scalacheck"         %% "scalacheck"          % "1.10.0" % "test",
    "org.scalanlp"           %% "breeze-math"         % "0.3-SNAPSHOT"
  )
).dependsOn (
  RootProject(uri("git://github.com/lancelet/scalasignal.git"))
)
