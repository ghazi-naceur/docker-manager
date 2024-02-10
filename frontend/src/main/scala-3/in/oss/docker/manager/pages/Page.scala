package in.oss.docker.manager.pages

import tyrian.*
import cats.effect.*
import in.oss.docker.manager.AppGUI
import in.oss.docker.manager.pages.Page.Message

abstract class Page {
  def initCmd: Cmd[IO, Message]
  def update(message: Message): (Page, Cmd[IO, Message])
  def view(): Html[Message]
}
object Page {
  trait Message extends AppGUI.Message

  object Urls {
    val CONTAINERS = "/containers"
    val HOME       = "/"
  }

  import Urls.*

  def get(location: String): Page = location match {
    case CONTAINERS => ContainersPage()
    case _          => NotFoundPage()
  }
}
