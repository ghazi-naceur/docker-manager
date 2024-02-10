package in.oss.docker.manager.pages
import cats.effect.IO
import in.oss.docker.manager.pages
import in.oss.docker.manager.domain.Container
import in.oss.docker.manager.pages.ContainersPage.{LoadContainers, Message}
import tyrian.{Cmd, Html}
import io.circe.parser.*
import io.circe.generic.auto.*
import tyrian.*
import tyrian.Html.*
import tyrian.http.*

case class Model(containers: List[Container])

final case class ContainersPage(containers: List[Container] = List()) extends Page {

  override def initCmd: Cmd[IO, Page.Message] = getContainersEndpoint

  override def update(message: Page.Message): (Page, Cmd[IO, Page.Message]) = message match {
    case ContainersPage.LoadContainers(cont) => (this.copy(containers = containers ++ cont), Cmd.None)
    case ContainersPage.NoOp                 => (this, Cmd.None)
    case ContainersPage.Error(error)         => (this, Cmd.None)
  }

  override def view(): Html[Page.Message] = {
    div(`class` := "container")(
      div(`class` := "row justify-content-center")(
        div(`class` := "col-14")(
          div(`class` := "p-4")(h2("Containers list")),
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
              for (container <- containers)
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
      )
    )
  }

  private def getContainersEndpoint: Cmd[IO, Message] = Http.send(
    Request.get("http://localhost:5555/docker/containers"),
    Decoder[Message](
      response =>
        parse(response.body).flatMap(_.as[List[Container]]) match {
          case Right(containers) => LoadContainers(containers)
          case Left(thr)         => ContainersPage.Error(thr.getMessage)
        },
      error => ContainersPage.Error(error.toString)
    )
  )
}

object ContainersPage {
  trait Message extends pages.Page.Message

  case object NoOp extends Message

  case class LoadContainers(containers: List[Container]) extends Message

  case class Error(error: String) extends Message
}
