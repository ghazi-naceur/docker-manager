package in.oss.docker.manager.pages

import tyrian.*
import cats.effect.*
import in.oss.docker.manager.pages.Page.Msg

abstract class Page {
  def initCmd: Cmd[IO, Msg]
  def update(msg: Msg): (Page, Cmd[IO, Msg])
  def view(): Html[Msg]
}
object Page {
  trait Msg

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
