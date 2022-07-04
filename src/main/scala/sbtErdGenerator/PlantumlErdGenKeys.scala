package sbtErdGenerator

import sbt._

trait PlantumlErdGenKeys {
  lazy val erdGenUsername = settingKey[String]("username.")
  lazy val erdGenPassword = settingKey[String]("password.")
  lazy val erdGenDriver = settingKey[String]("Jdbc Driver.")
  lazy val erdGenUrl = settingKey[String]("Jdbc URL to db.")
  lazy val erdTargetDir = settingKey[File]("target directory to store generated erd file.")

  lazy val erdGen = taskKey[Unit]("Generates zip file which includes all files from sourceZipDir")

}
