package in.oss.docker.manager.pages

import com.raquo.laminar.api.L.{*, given}
import com.raquo.laminar.nodes.ReactiveHtmlElement
import org.scalajs.dom
import frontroute.*
import org.scalajs.dom.HTMLElement

object Router {

  def apply(): ReactiveHtmlElement[HTMLElement] =
    mainTag(
      routes(
        div(
          cls := "container-fluid",
          (pathEnd | path("containers")) {
            ContainersPage()
          },
          path("images") {
            ImagesPage()
          },
          noneMatched {
            NotFoundPage()
          }
        )
      )
    )
}
