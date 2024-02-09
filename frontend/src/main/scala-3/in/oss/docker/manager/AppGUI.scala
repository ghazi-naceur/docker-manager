package in.oss.docker.manager

import cats.effect.*
import in.oss.docker.manager.domain.Container
import io.circe.parser.*
import io.circe.generic.auto.*
import tyrian.*
import tyrian.Html.*
import tyrian.http.*
import tyrian.{Cmd, Html, Sub, TyrianApp}

import scala.scalajs.js
import scala.scalajs.js.annotation.JSExportTopLevel

enum Message {
  case NoOp
  case LoadContainers(containers: List[Container])
  case Error(error: String)
}

case class Model(containers: List[Container])

@JSExportTopLevel("DockerManagerGUI")
object AppGUI extends TyrianApp[Message, Model] {
  override def init(flags: Map[String, String]): (Model, Cmd[IO, Message]) =
    (Model(List()), getContainersEndpoint)

  override def view(model: Model): Html[Message] = {
    div(
      h1("Containers"),
      div(
        model.containers.map { container =>
          div(
            div(label("==========================")),
            div(strong("Container ID: "), label(container.containerId.value)),
            div(strong("Command: "), label(container.command.value)),
            div(strong("Image: "), label(container.image.value)),
            div(strong("Ports: "), label(container.ports.value)),
            div(strong("Created: "), label(container.created.value)),
            div(strong("Names: "), label(container.names.value)),
            div(strong("Status: "), label(container.status.value))
          )
        }
      )
    )
  }

  override def update(model: Model): Message => (Model, Cmd[IO, Message]) = {
    case Message.LoadContainers(containers) => (model.copy(containers = model.containers ++ containers), Cmd.None)
    case Message.NoOp                       => (model, Cmd.None)
    case Message.Error(error)               => (model, Cmd.None)
  }

  override def subscriptions(model: Model): Sub[IO, Message] = Sub.None

  private def getContainersEndpoint: Cmd[IO, Message] = Http.send(
    Request.get("http://localhost:5555/docker/containers"),
    Decoder[Message](
      response =>
        parse(response.body).flatMap(_.as[List[Container]]) match {
          case Right(containers) => Message.LoadContainers(containers)
          case Left(thr)         => Message.Error(thr.getMessage)
        },
      error => Message.Error(error.toString)
    )
  )
}
