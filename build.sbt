import Dependencies._
import sbt.Keys.version

ThisBuild / scalaVersion := "2.12.8"
ThisBuild / organization := "io.github.y-sedira"
ThisBuild / organizationName := "y-sedira"
ThisBuild / homepage := Some(url("https://github.com/y-sedira/sbt-plantuml-erd-gen"))
ThisBuild / scmInfo := Some(ScmInfo(url("https://github.com/y-sedira/sbt-plantuml-erd-gen"), "git@github.com:y-sedira/sbt-plantuml-erd-gen.git"))
ThisBuild / developers := List(Developer("ysedira", "ysedira", "", url("https://github.com/y-sedira")))
ThisBuild / licenses +=  ("MIT", url("https://raw.githubusercontent.com/y-sedira/sbt-plantuml-erd-gen/main/LICENSE"))
ThisBuild / crossPaths := false


ThisBuild / pomIncludeRepository := { _ => false }
ThisBuild / sonatypeCredentialHost := "s01.oss.sonatype.org"
ThisBuild / sonatypeRepository := "https://s01.oss.sonatype.org/service/local"




lazy val root = (project in file("."))
  .enablePlugins(SbtPlugin)
  .settings(
    name := "sbt-plantuml-erd-gen",
    sbtPlugin := true,
    sbtVersion := "1.0.0",
    libraryDependencies += "org.postgresql" % "postgresql" % "42.3.6",
    libraryDependencies += "net.sourceforge.plantuml" % "plantuml" % "1.2022.6",

    scriptedLaunchOpts += ("-Dplugin.version=" + version.value),
    scriptedLaunchOpts ++= sys.process.javaVmArguments.filter(
      a => Seq("-Xmx", "-Xms", "-XX", "-Dsbt.log.noformat").exists(a.startsWith)
    ),
    scriptedBufferLog := false,
    sbtTestDirectory := sourceDirectory.value / "sbt-test",
  )
