object Versions extends WebJarsVersions with ScalaJSVersions with SharedVersions
{
	val scala = "2.11.8"

	val akkaHttp = "10.0.1"

	val bcrypt = "3.0"

	val ammonite = "0.7.4"

	val apacheCodec = "1.10"

	val akkaHttpExtensions = "0.0.15"

	val controls = "0.0.24"
}

trait ScalaJSVersions {

	val jqueryFacade = "1.0"

	val semanticUIFacade = "0.0.1"

	val dom = "0.9.1"

	val codemirrorFacade = "5.13.2-0.7"

}

//versions for libs that are shared between client and server
trait SharedVersions
{
	val scalaRx = "0.3.0"

	val scalaTags = "0.5.4"

	val scalaCSS = "0.5.1"

	val scalaTest =  "3.0.1"

	val macroParadise = "2.1.0"

}


trait WebJarsVersions{

	val jquery = "3.1.1"

	val semanticUI = "2.2.7"

	val codemirror = "5.22.0"

}

