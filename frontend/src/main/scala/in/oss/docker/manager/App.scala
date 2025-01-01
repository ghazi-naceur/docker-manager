package in.oss.docker.manager

import com.raquo.airstream.ownership.OneTimeOwner
import com.raquo.airstream.timing.PeriodicStream
import com.raquo.laminar.api.L.{*, given}
import com.raquo.laminar.nodes.ReactiveHtmlElement
import frontroute.LinkHandler
import in.oss.docker.manager.components.NavBar
import in.oss.docker.manager.pages.{ContainersPage, Router}
import org.scalajs.dom
import org.scalajs.dom.HTMLDivElement

import scala.util.Try

object App {

  def main(args: Array[String]): Unit = {
    val containerNode = dom.document.querySelector("#app")

    val router: ReactiveHtmlElement[HTMLDivElement] =
      div(
        NavBar(),
        Router()
      ).amend(LinkHandler.bind)

    render(
      containerNode,
      router
    )
  }
}
