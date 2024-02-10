package in.oss.docker.manager

import cats.effect.*
import in.oss.docker.manager.core.Router
import in.oss.docker.manager.core.Router.Msg
import in.oss.docker.manager.pages.Page
import io.circe.generic.auto.*
import org.scalajs.dom.window
import tyrian.*
import tyrian.Html.*
import tyrian.{Cmd, Html, Sub, TyrianApp}

import scala.scalajs.js
import scala.scalajs.js.annotation.JSExportTopLevel

case class Model(router: Router, page: Page)
@JSExportTopLevel("DockerManagerGUI")
class AppGUI extends TyrianApp[AppGUI.Msg, Model] {

  override def init(flags: Map[String, String]): (Model, Cmd[IO, AppGUI.Msg]) = {
    val location            = window.location.pathname
    val page                = Page.get(location)
    val pageCmd             = page.initCmd
    val (router, routerCmd) = Router.startAt(location)
    (Model(router, page), routerCmd |+| pageCmd)
  }

  override def update(model: Model): AppGUI.Msg => (Model, Cmd[IO, AppGUI.Msg]) = {
    case msg: Router.Msg =>
      val (newRouter, routerCmd) = model.router.update(msg)
      if (model.router == newRouter) (model, Cmd.None)
      else {
        val newPage    = Page.get(newRouter.location)
        val newPageCmd = newPage.initCmd
        (model.copy(router = newRouter, page = newPage), routerCmd |+| newPageCmd)
      }

    case msg: Page.Msg =>
      val (newPage, command) = model.page.update(msg)
      (model.copy(page = newPage), command)
  }

  override def view(model: Model): Html[AppGUI.Msg] = {
    div(
      renderNavLink("Containers", "/containers"),
      renderNavLink("Images", "/images"),
      model.page.view()
    )
  }

  override def subscriptions(model: Model): Sub[IO, Msg] = {
    Sub.make(
      "urlChange",
      model.router.history.state.discrete
        .map(_.get)
        .map(newLocation => Router.ChangeLocation(newLocation, browserTriggered = true))
    )
  }

  private def renderNavLink(text: String, location: String) = {
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
  }
}

object AppGUI {
  type Msg = Router.Msg | Page.Msg
}
