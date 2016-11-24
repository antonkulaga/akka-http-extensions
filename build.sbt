import com.typesafe.sbt.gzip.Import._
import com.typesafe.sbt.web._
import com.typesafe.sbt.web.pipeline.Pipeline
import play.twirl.sbt._
import playscalajs.{PlayScalaJS, ScalaJSPlay}
import sbt.Keys._
import sbt._
import spray.revolver.RevolverPlugin.autoImport._

lazy val bintrayPublishIvyStyle = settingKey[Boolean]("=== !publishMavenStyle") //workaround for sbt-bintray bug

lazy val publishSettings = Seq(
  bintrayRepository := "denigma-releases",

  bintrayOrganization := Some("denigma"),

  licenses += ("MPL-2.0", url("http://opensource.org/licenses/MPL-2.0")),

  bintrayPublishIvyStyle := true,

  scmInfo := Some(ScmInfo(url("https://github.com/antonkulaga/akka-http-extensions"), "git@github.com:antonkulaga/akka-http-extensions.git"))
)

/**
 * For parts of the project that we will not publish
 */
lazy val noPublishSettings = Seq(
  publish := (),
  publishLocal := (),
  publishArtifact := false
)


//settings for all the projects
lazy val commonSettings = Seq(
  scalaVersion := Versions.scala,
  organization := "org.denigma",
  resolvers += sbt.Resolver.bintrayRepo("denigma", "denigma-releases"), //for scala-js-binding
  libraryDependencies ++= Dependencies.commonShared.value ++ Dependencies.testing.value,
  updateOptions := updateOptions.value.withCachedResolution(true) //to speed up dependency resolution
)

lazy val extensions = project.in(file("extensions"))
  .settings(commonSettings++publishSettings: _*)
  .settings(
    name := "akka-http-extensions",
    version := Versions.akkaHttpExtensions,
    libraryDependencies ++= Dependencies.akka.value ++ Dependencies.otherJVM.value
  ).enablePlugins(BintrayPlugin).disablePlugins(RevolverPlugin)

val scalaJSDevStage  = Def.taskKey[Pipeline.Stage]("Apply fastOptJS on all Scala.js projects")

def scalaJSDevTaskStage: Def.Initialize[Task[Pipeline.Stage]] = Def.task { mappings: Seq[PathMapping] =>
  mappings ++ PlayScalaJS.devFiles(Compile).value ++ PlayScalaJS.sourcemapScalaFiles(fastOptJS).value
}


// Scala-Js preview frontend
lazy val frontend = project.in(file("preview/frontend"))
  .settings(commonSettings: _*)
  .settings(
    persistLauncher in Compile := true,
    persistLauncher in Test := false,
    jsDependencies += RuntimeDOM % "test",
    libraryDependencies ++= Dependencies.shared.value ++ Dependencies.sjsLibs.value
  ).enablePlugins(ScalaJSPlay).disablePlugins(RevolverPlugin)

//backend project for preview uhand testing
lazy val backend = Project("backend", file("preview/backend"),settings = commonSettings)
  .settings(
    mainClass in Compile := Some("org.denigma.preview.Main"),
    scalaJSDevStage := scalaJSDevTaskStage.value,
    (emitSourceMaps in fullOptJS) := true,
    libraryDependencies ++= Dependencies.shared.value  ++ Dependencies.webjars.value,
    pipelineStages in Assets := Seq(scalaJSDevStage, gzip), //for run configuration
    scalaJSProjects := Seq(frontend)
  ).enablePlugins(SbtTwirl, SbtWeb, PlayScalaJS).dependsOn(extensions).disablePlugins(RevolverPlugin).aggregate(extensions)

/** Scalatex banana-rdf website, see http://lihaoyi.github.io/Scalatex/#QuickStart */
lazy val readme = scalatex.ScalatexReadme(
  projectId = "readme",
  wd = file(""),
  url = "https://github.com/denigma/akka-http-extensions/tree/master",
  source = "Readme"
).disablePlugins(RevolverPlugin)


lazy val root = Project("root",file("."),settings = commonSettings).settings(
  mainClass in Compile := (mainClass in backend in Compile).value,
  (fullClasspath in Runtime) += (packageBin in backend in Assets).value
).dependsOn(backend).aggregate(backend, frontend)
