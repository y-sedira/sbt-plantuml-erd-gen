import Dependencies._
import sbt.Keys.version

ThisBuild / scalaVersion := "2.12.8"
ThisBuild / version := "0.1.0"
ThisBuild / organization := "io.ysedira"
ThisBuild / organizationName := "erdGenerator"

lazy val root = (project in file("."))
  .enablePlugins(SbtPlugin)
  .settings(
    name := "sbt-plantuml-erd-gen",
    sbtPlugin := true,
    sbtVersion := "1.0.0",
    //    libraryDependencies += scalaTest % Test,
    //    libraryDependencies += "org.slf4j" % "slf4j-nop" % "1.7.36",
    libraryDependencies += "org.postgresql" % "postgresql" % "42.2.16",

    scriptedLaunchOpts += ("-Dplugin.version=" + version.value),
    scriptedLaunchOpts ++= sys.process.javaVmArguments.filter(
      a => Seq("-Xmx", "-Xms", "-XX", "-Dsbt.log.noformat").exists(a.startsWith)
    ),
    scriptedBufferLog := false,
    sbtTestDirectory := sourceDirectory.value / "sbt-test"
  )
