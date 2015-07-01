import com.typesafe.sbt.SbtNativePackager.autoImport._
import com.typesafe.sbt.web.SbtWeb
import org.scalajs.sbtplugin.ScalaJSPlugin
import org.scalajs.sbtplugin.ScalaJSPlugin.autoImport._
import sbt.Keys._
import sbt._
import bintray._
import BintrayPlugin.autoImport._
import spray.revolver.RevolverPlugin._
import play.twirl.sbt._
import play.twirl.sbt.SbtTwirl.autoImport._
import com.typesafe.sbt.web.SbtWeb.autoImport._
import scalatex.ScalatexReadme

object Build extends PreviewBuild {

	lazy val root = Project("root",file("."),settings = commonSettings)
		.settings(
			mainClass in Compile := (mainClass in backend in Compile).value,
      libraryDependencies += "com.lihaoyi" % "ammonite-repl_2.11.6" %  Versions.ammonite,
			initialCommands in console := """ammonite.repl.Repl.run("")""" //better console
		) dependsOn backend aggregate(backend,frontend)
}

class PreviewBuild extends LibraryBuild
{

	// some useful UI controls + shared code
	lazy val controls = crossProject
	  .crossType(CrossType.Full)
	  .in(file("preview/controls"))
	  .settings(commonSettings++publishSettings: _*)
	  .settings(version := Versions.controls)
	  .jsSettings(libraryDependencies ++= Dependencies.sjsLibs.value++Dependencies.templates.value)
	  .settings(name := "binding-controls")
	  .enablePlugins(BintrayPlugin)

	lazy val controlsJVM = controls.jvm
	lazy val controlsJS = controls.js

	// Scala-Js preview frontend
	lazy val frontend = Project("frontend", file("preview/frontend"))
		.settings(commonSettings: _*)
		.settings(
		persistLauncher in Compile := true,
		persistLauncher in Test := false,
		jsDependencies += RuntimeDOM % "test"
	).enablePlugins(ScalaJSPlugin).dependsOn(controlsJS)

	//backend project for preview and testing
	lazy val backend = Project("backend", file("preview/backend"),settings = commonSettings++Revolver.settings)
		.settings(
			libraryDependencies ++= Dependencies.templates.value++Dependencies.webjars.value,
				mainClass in Compile :=Some("org.denigma.preview.Main"),
        mainClass in Revolver.reStart := Some("org.denigma.preview.Main"),
        resourceGenerators in Compile <+=  (fastOptJS in Compile in frontend,
				  packageScalaJSLauncher in Compile in frontend) map( (f1, f2) => Seq(f1.data, f2.data)),
			watchSources <++= (watchSources in frontend),
      (managedClasspath in Runtime) += (packageBin in Assets).value
		).enablePlugins(SbtTwirl,SbtWeb).dependsOn(controlsJVM,extensions).aggregate(extensions)

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
		libraryDependencies ++= Dependencies.commonShared.value++Dependencies.testing.value,
		updateOptions := updateOptions.value.withCachedResolution(true) //to speed up dependency resolution
	)

	lazy val extensions = Project("extensions", file("extensions"))
		.settings(commonSettings++publishSettings: _*)
		.settings(
			name := "akka-http-extensions",
			version := Versions.akkaHttpExtensions,
			libraryDependencies ++= Dependencies.akka.value ++ Dependencies.otherJVM.value
		).enablePlugins(BintrayPlugin)

}
