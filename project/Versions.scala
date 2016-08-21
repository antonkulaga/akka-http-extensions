object Versions extends WebJarsVersions with ScalaJSVersions with SharedVersions
{
	val scala = "2.11.8"

	val akkaHttp = "2.4.9"

	val bcrypt = "2.6"

	val ammonite = "0.7.4"

	val apacheCodec = "1.10"

	val akkaHttpExtensions = "0.0.14"

	val controls = "0.0.20"
}

trait ScalaJSVersions {

	val jqueryFacade = "0.11"

	val semanticUIFacade = "0.0.1"

	val dom = "0.9.1"

	val codemirrorFacade = "5.13.2-0.7"

}

//versions for libs that are shared between client and server
trait SharedVersions
{
	val scalaRx = "0.3.0"

	val scalaTags = "0.5.4"

	val scalaCSS = "0.4.1"

	val scalaTest =  "3.0.0"

	val macroParadise = "2.1.0"

}


trait WebJarsVersions{

	val jquery = "3.1.0"

	val semanticUI = "2.2.2"

	val codemirror = "5.13.2"

}

