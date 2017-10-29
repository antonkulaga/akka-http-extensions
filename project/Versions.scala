object Versions extends WebJarsVersions with ScalaJSVersions with SharedVersions
{
	val scala = "2.12.3"

	val akkaHttp = "10.0.10"

	val bcrypt = "3.1"

	val ammonite = "1.0.0"

	val apacheCodec = "1.10"

	val akkaHttpExtensions = "0.0.16"

	val controls = "0.0.27"
}

trait ScalaJSVersions {

	val jqueryFacade = "1.0"

	val semanticUIFacade = "0.0.1"

	val dom = "0.9.2"

	val codemirrorFacade = "5.22.0-0.8"

}

//versions for libs that are shared between client and server
trait SharedVersions
{
	val scalaRx = "0.3.2"

	val scalaTags = "0.6.2"

	val scalaCSS = "0.5.3"

	val scalaTest =  "3.0.4"

	val macroParadise = "2.1.0"

}


trait WebJarsVersions{

	val jquery = "3.2.1"

	val semanticUI = "2.2.10"

	val codemirror = "5.24.2"

}

