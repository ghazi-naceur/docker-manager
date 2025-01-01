package in.oss.docker.manager.components

import com.raquo.laminar.api.L.*
import com.raquo.laminar.nodes.ReactiveHtmlElement
import org.scalajs.dom
import org.scalajs.dom.HTMLDivElement

object NavBar {

  def apply(): ReactiveHtmlElement[HTMLDivElement] =
    div(
      cls := "navbar navbar-expand-lg navbar-light bg-light",
      div(
        cls := "container-fluid",
        a(
          cls  := "navbar-brand",
          href := "/",
          "Docker Manager"
        ),
        button(
          cls                   := "navbar-toggler",
          `type`                := "button",
          dataAttr("bs-toggle") := "collapse",
          dataAttr("bs-target") := "#navbarNav",
          span(
            cls := "navbar-toggler-icon"
          )
        ),
        div(
          cls    := "collapse navbar-collapse",
          idAttr := "navbarNav",
          ul(
            cls := "navbar-nav",
            li(
              cls := "nav-item",
              a(
                cls          := "nav-link active",
                aria.current := "page",
                href         := "/containers",
                "Containers"
              )
            )
          )
        )
      )
    )
}
