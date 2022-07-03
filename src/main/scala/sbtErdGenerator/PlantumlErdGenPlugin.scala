package sbtErdGenerator

import sbt.Keys._
import sbt._
import _root_.io.ysedira.erdGenerator.DatabaseGenerator

object PlantumlErdGenPlugin extends AutoPlugin {
  override val trigger: PluginTrigger = noTrigger
  override val requires: Plugins = plugins.JvmPlugin

  object autoImport extends PlantumlErdGenKeys

  import autoImport._

  override lazy val projectSettings: Seq[Setting[_]] = Seq(
    targetZipDir := target.value / "zip",
    zip := plantumlErdGenTask.value
  )

  private def plantumlErdGenTask = Def.task {
    val log = sLog.value
    log.info("Generating...")
    DatabaseGenerator.main()
  }
}
