import bintray._
import bintray.BintrayPlugin.autoImport._
import com.typesafe.sbt.gzip.Import._
import com.typesafe.sbt.web.SbtWeb.autoImport._
import com.typesafe.sbt.web._
import com.typesafe.sbt.web.pipeline.Pipeline
import org.scalajs.sbtplugin.ScalaJSPlugin.autoImport._
import play.twirl.sbt._
import playscalajs.PlayScalaJS.autoImport._
import playscalajs.ScalaJSPlay.autoImport._
import playscalajs.{PlayScalaJS, ScalaJSPlay}
import sbt.Keys._
import sbt._
import spray.revolver.RevolverPlugin._

object Build extends PreviewBuild {

	lazy val root = Project("root",file("."),settings = commonSettings)
		.settings(
			mainClass in Compile := (mainClass in backend in Compile).value
		) dependsOn backend aggregate(backend,frontend)
}

class PreviewBuild extends LibraryBuild
{

	val scalaJSDevStage  = Def.taskKey[Pipeline.Stage]("Apply fastOptJS on all Scala.js projects")

	def scalaJSDevTaskStage: Def.Initialize[Task[Pipeline.Stage]] = Def.task { mappings: Seq[PathMapping] =>
		mappings ++ PlayScalaJS.devFiles(Compile).value ++ PlayScalaJS.sourcemapScalaFiles(fastOptJS).value
	}


	// some useful UI controls + shared code
	lazy val controls = crossProject
	  .crossType(CrossType.Full)
	  .in(file("preview/controls"))
	  .settings(commonSettings++publishSettings: _*)
	  .settings(
			version := Versions.controls,
			name := "binding-controls"
		)
		.jvmSettings(
			libraryDependencies ++= Dependencies.webjars.value
		)
		.jsSettings(
			libraryDependencies ++= Dependencies.sjsLibs.value,
			jsDependencies += RuntimeDOM % "test"
		)
		.jvmConfigure(p=>p.enablePlugins(SbtTwirl,SbtWeb).dependsOn(extensions))
		.enablePlugins(BintrayPlugin)

	lazy val controlsJVM = controls.jvm
	lazy val controlsJS = controls.js

	// Scala-Js preview frontend
	lazy val frontend = Project("frontend", file("preview/frontend"))
		.settings(commonSettings: _*)
		.settings(
		persistLauncher in Compile := true,
		persistLauncher in Test := false,
		jsDependencies += RuntimeDOM % "test",
		sourceMapsDirectories :=Seq( (baseDirectory in controlsJS).value )
		).enablePlugins(ScalaJSPlay).dependsOn(controlsJS)

	//backend project for preview uhand testing
	lazy val backend = Project("backend", file("preview/backend"),settings = commonSettings++Revolver.settings)
		.settings(
				mainClass in Compile :=Some("org.denigma.preview.Main"),
        mainClass in Revolver.reStart := Some("org.denigma.preview.Main"),
				scalaJSDevStage := scalaJSDevTaskStage.value,
				//pipelineStages := Seq(scalaJSProd,gzip),
				(emitSourceMaps in fullOptJS) := true,
				pipelineStages in Assets := Seq(scalaJSDevStage,gzip), //for run configuration
				(managedClasspath in Runtime) += (packageBin in Assets).value, //to package production deps
				scalaJSProjects := Seq(frontend)
		).enablePlugins(SbtTwirl,SbtWeb,PlayScalaJS).dependsOn(controlsJVM,extensions).aggregate(extensions)

	/** Scalatex banana-rdf website, see http://lihaoyi.github.io/Scalatex/#QuickStart */
	lazy val readme = scalatex.ScalatexReadme(
		projectId = "readme",
		wd = file(""),
		url = "https://github.com/denigma/akka-http-extensions/tree/master",
		source = "Readme"
	)

}

class LibraryBuild  extends sbt.Build{
	self=>

	protected lazy val bintrayPublishIvyStyle = settingKey[Boolean]("=== !publishMavenStyle") //workaround for sbt-bintray bug

	lazy val publishSettings = Seq(
		bintrayRepository := "denigma-releases",

		bintrayOrganization := Some("denigma"),

		licenses += ("MPL-2.0", url("http://opensource.org/licenses/MPL-2.0")),

		bintrayPublishIvyStyle := true
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
		libraryDependencies ++= Dependencies.shared.value ++ Dependencies.testing.value,
		updateOptions := updateOptions.value.withCachedResolution(true) //to speed up dependency resolution
	)

	lazy val extensions = Project("extensions", file("extensions"))
		.settings(commonSettings++publishSettings: _*)
		.settings(
			name := "akka-http-extensions",
			version := Versions.akkaHttpExtensions,
			libraryDependencies ++= Dependencies.akka.value ++ Dependencies.otherJVM.value,
			libraryDependencies += "com.lihaoyi" % "ammonite-repl" % "0.4.5" cross CrossVersion.full,
			initialCommands in console := """ammonite.repl.Repl.run("")""" //better console
		).enablePlugins(BintrayPlugin)

}
