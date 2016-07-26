import org.scalajs.sbtplugin.ScalaJSPlugin.AutoImport._

// Sub-project containing Activiti customizations (e.g. code behind service tasks)
lazy val activitiCustom = Project(id = "activiti-custom", base = file("activiti-custom"))
  .settings(
    name := Settings.activitiCustomName,
    version := Settings.version,
    scalaVersion := Settings.versions.scala,
    scalacOptions ++= Settings.scalacOptions,
    libraryDependencies ++= Settings.activitiDependencies.value
  )

// Sub-project containing Scala.js client web application
lazy val client = (project in file("client"))
  .enablePlugins(ScalaJSPlugin)
  .settings(
    name := Settings.clientName,
    version := Settings.version,
    scalaVersion := Settings.versions.scala,
    scalacOptions ++= Settings.scalacOptions,

    // Scala.js dependencies
    libraryDependencies ++= Settings.clientDependencies.value,

    // JS dependencies needed at runtime
    jsDependencies ++= Settings.clientJsDependencies.value,

    // yes, we want to package JS dependencies
    skip in packageJSDependencies := false,

    // use launcher code to start the client app (see launcher.js in index.html)
    persistLauncher := true,
    persistLauncher in Test := false,

    // make referenced paths in source maps relative to target path
    relativeSourceMaps := true
  )

// Root project contains no sources, but hosts our custom command
lazy val root = project.in(file("."))
  .settings(
    commands += dockerize
  )

// Custom command which prepares all artifacts necessary to build our docker image
lazy val dockerize = Command.command("dockerize") {
  state =>
    "activiti-custom/package" ::
      "client/fastOptJS" ::
      state
}