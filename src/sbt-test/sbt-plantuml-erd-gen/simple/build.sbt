import sbtErdGenerator.PlantumlErdGenPlugin
import sbtErdGenerator.PlantumlErdGenPlugin._
//import sbtErdGenerator.PlantumlErdGenKeys.sourceZipDir

lazy val root = (project in file("."))
  .enablePlugins(PlantumlErdGenPlugin)
  .settings(
    scalaVersion := "2.12.4",
    version := "0.1",
    sourceZipDir := crossTarget.value,
    libraryDependencies += "org.postgresql" % "postgresql" % "42.2.16"
  )