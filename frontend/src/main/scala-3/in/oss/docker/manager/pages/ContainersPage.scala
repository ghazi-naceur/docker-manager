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

final case class ContainersPage(backendHost: String, containers: List[Container] = List()) extends Page {

  override def initCmd: Cmd[IO, Page.Message] = getContainersEndpoint

  override def update(message: Page.Message): (Page, Cmd[IO, Page.Message]) = message match {
    case ContainersPage.LoadContainers(cont) => (this.copy(containers = containers ++ cont), Cmd.None)
    case ContainersPage.NoOp                 => (this, Cmd.None)
    case ContainersPage.Error(error)         => (this, Cmd.None)
  }

  override def view(): Html[Page.Message] = {
    div(`class` := "col-md-11")(
      div(h2("Containers list")),
      table(`class` := "table")(
        thead(
          tr(
            th("Container ID"),
            th("Image"),
            th("Command"),
            th("Created"),
            th("Status"),
            th("Ports"),
            th("Names"),
            th("Size")
          )
        ),
        tbody(
          for (container <- containers)
            yield tr(
              td(`class` := "align-middle")(container.containerId.value),
              td(`class` := "align-middle")(container.imageName.value),
              td(`class` := "align-middle")(container.command.value),
              td(`class` := "align-middle")(container.created.value),
              td(`class` := "align-middle")(container.status.value),
              td(`class` := "align-middle")(container.ports.value),
              td(`class` := "align-middle")(container.names.value),
              td(`class` := "align-middle")(container.size.value)
            )
        )
      )
    )
  }

  private def getContainersEndpoint: Cmd[IO, Message] = {
    Http.send(
      Request.get(s"$backendHost/docker/containers"),
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
}

object ContainersPage {
  trait Message extends pages.Page.Message

  case object NoOp extends Message

  case class LoadContainers(containers: List[Container]) extends Message

  case class Error(error: String) extends Message

  case class Model(containers: List[Container])
}