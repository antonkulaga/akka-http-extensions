object Versions extends WebJarsVersions with ScalaJSVersions with SharedVersions
{
	val scala = "2.11.8"

	val akkaHttp = "2.4.5"

	val bcrypt = "2.6"

	val ammonite = "0.5.4"

	val apacheCodec = "1.10"

	val akkaHttpExtensions = "0.0.12"

	val controls = "0.0.15"
}

trait ScalaJSVersions {

	val jqueryFacade = "0.11"

	val semanticUIFacade = "0.0.1"

	val dom = "0.9.0"

	val codemirrorFacade = "5.11-0.7"

}

//versions for libs that are shared between client and server
trait SharedVersions
{
	val scalaRx = "0.3.0"

	val scalaTags = "0.5.4"

	val scalaCSS = "0.4.1"

	val productCollections = "1.4.2"

	val scalaTest =  "3.0.0-M16-SNAP4"

	val scalaTestMatchers = "3.0.0-SNAP13"

	val macroParadise = "2.1.0"

}


trait WebJarsVersions{

	val jquery = "2.2.3"

	val semanticUI = "2.1.8"

	val codemirror = "5.13.2"

}

