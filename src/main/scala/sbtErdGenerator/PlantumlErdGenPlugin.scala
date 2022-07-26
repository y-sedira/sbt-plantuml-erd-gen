package sbtErdGenerator

import sbt.Keys._
import sbt.{Def, _}
import _root_.io.ysedira.erdGenerator.DatabaseGenerator

object PlantumlErdGenPlugin extends AutoPlugin {
  override val trigger: PluginTrigger = noTrigger
  override val requires: Plugins = plugins.JvmPlugin

  object autoImport extends PlantumlErdGenKeys

  import autoImport._

  override lazy val projectSettings: Seq[Setting[_]] = Seq(
    erdGen := plantumlErdGenTask.value
  )

  private def plantumlErdGenTask: Def.Initialize[Task[Unit]] = Def.taskDyn[Unit] {
    val log = sLog.value
    log.info("Generating...")
    val target = erdTargetDir.value
    val isDir = target.isDirectory
    if (isDir) {
      Def.task {
        DatabaseGenerator.generate(erdGenUrl.value, erdGenDriver.value, erdGenUsername.value, erdGenPassword.value)(target)
      }
    } else {
      Def.task {
        log.error("erdTargetDir is a not a directory")
      }
    }
  }
}
