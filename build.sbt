import Dependencies._

ThisBuild / scalaVersion := "2.13.8"
ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / organization := "com.example"
ThisBuild / organizationName := "example"

lazy val root = (project in file("."))
  .settings(
    name := "erd-generator",
    libraryDependencies += scalaTest % Test,
    libraryDependencies += "com.typesafe.slick" %% "slick" % "3.3.3",
    libraryDependencies += "com.typesafe.slick" %% "slick-codegen" % "3.3.3",
    libraryDependencies += "org.slf4j" % "slf4j-nop" % "1.7.36",
    libraryDependencies += "org.postgresql" % "postgresql" % "42.2.16"
  )

(Compile / sourceGenerators) += slick.taskValue // Automatic code generation on build

lazy val slick = taskKey[Seq[File]]("Generate Tables.scala")
slick := {
  val dir = (Compile / sourceManaged) value
  val outputDir = dir / "plantuml"
  val url = "jdbc:postgresql://localhost:5433/dev" // connection info
  val jdbcDriver = "org.postgresql.Driver"
  val slickDriver = "slick.jdbc.PostgresProfile"
  val pkg = "demo"

  val cp = (Compile / dependencyClasspath) value
  val s = streams value

  runner.value.run("example.ErdRunner",
    cp.files,
    Array(slickDriver, jdbcDriver, url, outputDir.getPath, pkg,"dev","dev"),
    s.log).failed foreach (sys error _.getMessage)

  val file = outputDir / pkg / "Tables.scala"

  Seq(file)
}


// See https://www.scala-sbt.org/1.x/docs/Using-Sonatype.html for instructions on how to publish to Sonatype.