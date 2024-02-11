package in.oss.docker.manager

import cats.effect.*
import in.oss.docker.manager.AppGUI.{Message, Model}
import in.oss.docker.manager.core.Router
import in.oss.docker.manager.pages.Page
import io.circe.generic.auto.*
import org.scalajs.dom.window
import tyrian.*
import tyrian.Html.*
import tyrian.{Cmd, Html, Sub, TyrianApp}

import scala.scalajs.js
import scala.scalajs.js.annotation.JSExportTopLevel

@JSExportTopLevel("DockerManagerGUI")
class AppGUI extends TyrianApp[Message, Model] {

  override def init(flags: Map[String, String]): (Model, Cmd[IO, Message]) = {
    val location            = window.location.pathname
    val page                = Page.get(location)
    val pageCmd             = page.initCmd
    val (router, routerCmd) = Router.startAt(location)
    (Model(router, page), routerCmd |+| pageCmd)
  }

  override def update(model: Model): Message => (Model, Cmd[IO, Message]) = {
    case message: Router.Message =>
      val (newRouter, routerCmd) = model.router.update(message)
      if (model.router == newRouter) (model, Cmd.None)
      else {
        val newPage    = Page.get(newRouter.location)
        val newPageCmd = newPage.initCmd
        (model.copy(router = newRouter, page = newPage), routerCmd |+| newPageCmd)
      }

    case message: Page.Message =>
      val (newPage, command) = model.page.update(message)
      (model.copy(page = newPage), command)
  }

  override def view(model: Model): Html[Message] = {
    div(cls := "container-fluid full-width-row")(
      div(`class` := "row full-width-row")(
        div(`class` := "col-md-2 full-width-row")(
          renderNavLink("Containers", "/containers"),
          renderNavLink("Images", "/images")
        ),
        div(`class` := "col-md-10 full-width-row")(
          model.page.view()
        )
      )
    )
  }

  override def subscriptions(model: Model): Sub[IO, Message] = {
    Sub.make(
      "urlChange",
      model.router.history.state.discrete
        .map(_.get)
        .map(newLocation => Router.ChangeLocation(newLocation, browserTriggered = true))
    )
  }

  private def renderNavLink(text: String, location: String) = {
    div(cls := "p-3")(
      a(
        href    := location,
        `class` := "nav-link",
        onEvent(
          "click",
          e => {
            e.preventDefault()
            Router.ChangeLocation(location)
          }
        )
      )(text)
    )
  }
}

object AppGUI {
  trait Message
  case class Model(router: Router, page: Page)
}
