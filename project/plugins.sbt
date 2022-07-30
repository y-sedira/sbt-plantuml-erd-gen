// sbt 1 only, see FAQ for 0.13 support
addSbtPlugin("com.github.sbt" % "sbt-ci-release" % "1.5.10")
libraryDependencies += "org.scala-sbt" %% "scripted-plugin" % sbtVersion.value
