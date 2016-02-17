object Versions extends WebJarsVersions with ScalaJSVersions with SharedVersions
{
	val scala = "2.11.7"

	val akkaHttp = "2.0.3"

	val bcrypt = "2.5"

	val ammonite = "0.5.2"

	val apacheCodec = "1.10"

	val akkaHttpExtensions = "0.0.10"

	val controls = "0.0.10"
}

trait ScalaJSVersions {

	val jqueryFacade = "0.11"

	val semanticUIFacade = "0.0.1"

	val dom = "0.9.0"

	val codemirrorFacade = "5.5-0.5"

	val binding = "0.8.2"

}

//versions for libs that are shared between client and server
trait SharedVersions
{
	val scalaRx = "0.3.0"

	val scalaTags = "0.5.4"

	val scalaCSS = "0.4.0"

	val productCollections = "1.4.2"

	val scalaTest =  "3.0.0-SNAP13"

	val macroParadise = "2.1.0"

}


trait WebJarsVersions{

	val jquery = "2.2.0"

	val semanticUI = "2.1.8"

	val codemirror = "5.11"

}

