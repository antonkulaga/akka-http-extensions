import com.typesafe.sbt.SbtNativePackager.autoImport._
import com.typesafe.sbt.web.SbtWeb
import com.typesafe.sbt.web.SbtWeb.autoImport._
import org.scalajs.sbtplugin.ScalaJSPlugin.autoImport._
import sbt.Keys._
import sbt._

lazy val bintrayPublishIvyStyle = settingKey[Boolean]("=== !publishMavenStyle") //workaround for sbt-bintray bug

lazy val publishSettings = Seq(
  bintrayRepository := "denigma-releases",

  bintrayOrganization := Some("denigma"),

  licenses += ("MPL-2.0", url("http://opensource.org/licenses/MPL-2.0")),

  bintrayPublishIvyStyle := true,

  scmInfo := Some(ScmInfo(url("https://github.com/antonkulaga/akka-http-extensions"), "git@github.com:antonkulaga/akka-http-extensions.git"))
)

//settings for all the projects
lazy val commonSettings = Seq(
  scalaVersion := Versions.scala,
  organization := "org.denigma",
  resolvers += sbt.Resolver.bintrayRepo("denigma", "denigma-releases"), //for scala-js-binding
  libraryDependencies ++= Dependencies.commonShared.value ++ Dependencies.testing.value,
  crossScalaVersions := Seq("2.11.11", "2.12.4"),
  updateOptions := updateOptions.value.withCachedResolution(true) //to speed up dependency resolution
)

lazy val extensions = project.in(file("extensions"))
  .settings(commonSettings++publishSettings: _*)
  .settings(
    name := "akka-http-extensions",
    version := Versions.akkaHttpExtensions,
    libraryDependencies ++= Dependencies.akka.value ++ Dependencies.otherJVM.value
  ).enablePlugins(BintrayPlugin).disablePlugins(RevolverPlugin)

// Scala-Js preview frontend
lazy val frontend = project.in(file("preview/frontend"))
  .settings(commonSettings: _*)
  .settings(
    persistLauncher in Compile := true,
    persistLauncher in Test := false,
    jsEnv := new org.scalajs.jsenv.jsdomnodejs.JSDOMNodeJSEnv,
    libraryDependencies ++= Dependencies.shared.value ++ Dependencies.sjsLibs.value
  ).enablePlugins(ScalaJSPlugin, ScalaJSWeb).disablePlugins(RevolverPlugin)

//backend project for preview uhand testing
lazy val backend = (project in file("preview/backend")).settings(commonSettings)
  .settings(
    mainClass in Compile := Some("org.denigma.preview.Main"),
    (emitSourceMaps in fullOptJS) := true,
    libraryDependencies ++= Dependencies.shared.value  ++ Dependencies.webjars.value,
    scalaJSProjects := Seq(frontend),
    pipelineStages in Assets := Seq(scalaJSPipeline)
  ).enablePlugins(SbtTwirl, SbtWeb).dependsOn(extensions).disablePlugins(RevolverPlugin).aggregate(extensions)

/** Scalatex banana-rdf website, see http://lihaoyi.github.io/Scalatex/#QuickStart */
lazy val readme = scalatex.ScalatexReadme(
  projectId = "readme",
  wd = file(""),
  url = "https://github.com/denigma/akka-http-extensions/tree/master",
  source = "Readme"
).disablePlugins(RevolverPlugin)


lazy val root = (project in file(".")).settings(commonSettings)
.settings(
  mainClass in Compile := (mainClass in backend in Compile).value,
  (fullClasspath in Runtime) += (packageBin in backend in Assets).value
).dependsOn(backend).aggregate(backend, frontend)
