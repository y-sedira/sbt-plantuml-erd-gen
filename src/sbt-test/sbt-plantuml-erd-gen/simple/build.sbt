import sbtErdGenerator.PlantumlErdGenPlugin
import sbtErdGenerator.PlantumlErdGenPlugin._

lazy val root = (project in file("."))
  .enablePlugins(PlantumlErdGenPlugin)
  .settings(
    scalaVersion := "2.12.4",
    version := "0.1",
    erdGenUsername := "world",
    erdGenPassword := "world123",
    erdGenDriver := "org.postgresql.Driver",
    erdGenUrl := "jdbc:postgresql://localhost:5433/world-db",
    erdTargetDir := new sbt.File("erdGen.puml"),
    libraryDependencies += "org.postgresql" % "postgresql" % "42.2.16"
  )