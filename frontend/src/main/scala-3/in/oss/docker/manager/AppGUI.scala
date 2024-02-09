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
      table(`class` := "table")(
        thead(
          tr(
            th("Container ID"),
            th("Command"),
            th("Image"),
            th("Ports"),
            th("Created"),
            th("Names"),
            th("Status")
          )
        ),
        tbody(
          for (container <- model.containers)
            yield tr(
              td(`class` := "align-middle")(container.containerId.value),
              td(`class` := "align-middle")(container.command.value),
              td(`class` := "align-middle")(container.image.value),
              td(`class` := "align-middle")(container.ports.value),
              td(`class` := "align-middle")(container.created.value),
              td(`class` := "align-middle")(container.names.value),
              td(`class` := "align-middle")(container.status.value)
            )
        )
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
