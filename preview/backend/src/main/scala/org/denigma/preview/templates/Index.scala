package org.denigma.preview.templates

import scalacss.Defaults._
import scalacss.ScalatagsCss._
import scalatags.Text._
import scalatags.Text.all._

object Index {

  lazy val content = html(
    head(
      title := "Hello world!",
      link(rel := "stylesheet", href := "mystyles.css")
    ),
    body(
      h1("Hello World!"),
      div(
        p(`class`:="desc","This project will be used to build a plasmid bank!"),
      script(src:="resources/frontend-fastopt.js"),
      script(src:="resources/frontend-launcher.js")
      )
   )
  )

  lazy val template = content.render

}