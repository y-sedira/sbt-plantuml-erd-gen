//import ErdGenerator.autoImport.hello
//import sbt.AutoPlugin
//import sbt._
//import Keys._
//import complete.DefaultParsers._
//
//object ErdGenerator extends AutoPlugin {
//  // by defining autoImport, the settings are automatically imported into user's `*.sbt`
//  object autoImport {
//    // configuration points, like the built-in `version`, `libraryDependencies`, or `compile`
//    val generateErd = taskKey[String]("Generate ERD ")
//    val generateErdUsername = settingKey[String]("Username to use when connecting to the db.")
//    val hello = inputKey[Unit]("Say hello!")
//
//  }
//
//  override lazy val projectSettings = Seq(
//    hello := {
//      val args = spaceDelimited("").parsed
//      println(s"Hello, ${args(0)}")
//    }
//  )
//}
