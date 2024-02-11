package in.oss.docker.manager.pages

import tyrian.*
import cats.effect.*
import in.oss.docker.manager.AppGUI
import in.oss.docker.manager.pages.Page.Message
import org.scalajs.dom.window

import scala.scalajs.LinkingInfo

abstract class Page {
  def initCmd: Cmd[IO, Message]
  def update(message: Message): (Page, Cmd[IO, Message])
  def view(): Html[Message]
}
object Page {
  trait Message extends AppGUI.Message

  object Urls {
    val CONTAINERS = "/containers"
    val IMAGES     = "/images"
    val HOME       = "/"
  }

  import Urls.*

  private val backendHost: String =
    if (LinkingInfo.developmentMode) "http://localhost:6543"
    else window.location.origin.get

  def get(location: String): Page = location match {
    case CONTAINERS => ContainersPage(backendHost)
    case IMAGES     => ImagesPage(backendHost)
    case _          => NotFoundPage()
  }
}
