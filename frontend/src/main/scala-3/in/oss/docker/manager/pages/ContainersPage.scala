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

final case class ContainersPage(
    backendHost: String,
    currentPage: Int = 1,
    itemsPerPage: Int = 10,
    containers: List[Container] = List()
) extends Page {

  override def initCmd: Cmd[IO, Page.Message] = getContainersEndpoint

  override def update(message: Page.Message): (Page, Cmd[IO, Page.Message]) = message match {
    case ContainersPage.LoadContainers(cont) => (this.copy(containers = containers ++ cont), Cmd.None)
    case ContainersPage.GoToPage(pageNumber) => (this.copy(currentPage = pageNumber), Cmd.None)
    case ContainersPage.NoOp                 => (this, Cmd.None)
    case ContainersPage.Error(error)         => (this, Cmd.None)
  }

  override def view(): Html[Page.Message] = {
    val totalPages = (this.containers.length.toDouble / this.itemsPerPage).ceil.toInt
    val startIndex = (this.currentPage - 1) * this.itemsPerPage
    val endIndex   = startIndex + this.itemsPerPage
    val pageItems  = this.containers.slice(startIndex, endIndex)

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
          for (container <- pageItems)
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
      ),
      div(cls := "d-flex justify-content-center mt-3")(
        (for (page <- 1 to totalPages) yield {
          button(
            cls := "btn btn-secondary mx-1" + (if (page == this.currentPage) " active" else ""),
            onClick(ContainersPage.GoToPage(page))
          )(page.toString)
        }).toList
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

  case class GoToPage(pageNumber: Int) extends Message

  case class Model(containers: List[Container])
}
