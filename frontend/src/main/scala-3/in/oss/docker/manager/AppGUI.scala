package in.oss.docker.manager

import cats.effect.IO
import in.oss.docker.manager.AppGUI.{Message, Model}
import tyrian.*
import tyrian.Html.*
import tyrian.{Cmd, Html, Sub, TyrianApp}

import scala.scalajs.js.annotation.JSExportTopLevel

@JSExportTopLevel("DockerManagerGUI")
class AppGUI extends TyrianApp[Message, Model] {
  override def init(flags: Map[String, String]): (Model, Cmd[IO, Message]) =
    (Model(""), Cmd.None)

  override def update(model: Model): Message => (Model, Cmd[IO, Message]) = ???

  override def view(model: Model): Html[Message] =
    div(
      p("Docker Manager")
    )

  override def subscriptions(model: Model): Sub[IO, Message] = ???
}

object AppGUI {
  sealed trait Message
  case class Model(data: String)
}
