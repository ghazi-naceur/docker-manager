package in.oss.docker.manager.pages
import cats.effect.IO
import in.oss.docker.manager.pages
import in.oss.docker.manager.domain.{Container, ContainerID}
import in.oss.docker.manager.pages.ContainersPage.*
import tyrian.{Cmd, Html}
import io.circe.parser.*
import io.circe.generic.auto.*
import tyrian.*
import tyrian.Html.*
import tyrian.http.*
import tyrian.http.Method.*

final case class ContainersPage(
    backendHost: String,
    currentPage: Int = 1,
    itemsPerPage: Int = 10,
    containers: List[Container] = List(),
    errorMessage: String = ""
) extends Page {

  override def initCmd: Cmd[IO, Page.Message] = getContainersEndpoint

  override def update(message: Page.Message): (Page, Cmd[IO, Page.Message]) = message match {
    case LoadContainers(cont)               => (this.copy(containers = containers ++ cont, errorMessage = ""), Cmd.None)
    case GoToPage(pageNumber)               => (this.copy(currentPage = pageNumber, errorMessage = ""), Cmd.None)
    case AttemptStopContainer(containerID)  => (this, stopContainer(containerID))
    case ContainerStopped(container)        => (this.copy(containers = refreshContainers(container), errorMessage = ""), Cmd.None)
    case AttemptStartContainer(containerID) => (this, startContainer(containerID))
    case ContainerStarted(container)        => (this.copy(containers = refreshContainers(container), errorMessage = ""), Cmd.None)
    case AttemptRemoveContainer(containerID)   => (this, removeContainer(containerID))
    case ContainerRemoved(remainingContainers) => (this.copy(containers = remainingContainers, errorMessage = ""), Cmd.None)
    case NoOp                                  => (this, Cmd.None)
    case Error(error)                          => (this.copy(errorMessage = error), Cmd.None)
  }

  private def refreshContainers(container: Container): List[Container] = {
    containers.filterNot(_.containerId == container.containerId) :+ container
  }

  override def view(): Html[Page.Message] = {
    val totalPages = (this.containers.length.toDouble / this.itemsPerPage).ceil.toInt
    val startIndex = (this.currentPage - 1) * this.itemsPerPage
    val endIndex   = startIndex + this.itemsPerPage
    val pageItems  = this.containers.slice(startIndex, endIndex)

    div(
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
              th("Size"),
              th("Actions")
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
                td(`class` := "align-middle")(container.size.value),
                td(`class` := "align-middle")(
                  div(`class` := "dropdown")(
                    button(
                      `class` := "btn btn-secondary dropdown-toggle",
                      `type`  := "button",
                      id      := "dropdownMenuButton",
                      attribute("data-bs-toggle", "dropdown"),
                      attribute("aria-expanded", "false")
                    )("Choose action"),
                    div(`class` := "dropdown-menu", attribute("aria-labelledby", "dropdownMenuButton"))(
                      a(`class` := "dropdown-item", onClick(AttemptStopContainer(container.containerId)))("Stop"),
                      a(`class` := "dropdown-item", onClick(AttemptStartContainer(container.containerId)))("Start"),
                      a(`class` := "dropdown-item", onClick(AttemptRemoveContainer(container.containerId)))("Remove")
                    )
                  )
                )
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
      ),
      div(`class` := "col-md-11")(label(errorMessage))
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

  private def stopContainer(containerID: ContainerID): Cmd[IO, Message] = {
    Http.send(
      Request(method = Put, url = s"$backendHost/docker/container/${containerID.value}/stop"),
      Decoder[Message](
        response =>
          parse(response.body).flatMap(_.as[Container]) match {
            case Right(container) => ContainerStopped(container)
            case Left(thr)        => ContainersPage.Error(thr.getMessage)
          },
        error => ContainersPage.Error(error.toString)
      )
    )
  }

  private def startContainer(containerID: ContainerID): Cmd[IO, Message] = {
    Http.send(
      Request(method = Put, url = s"$backendHost/docker/container/${containerID.value}/start"),
      Decoder[Message](
        response =>
          parse(response.body).flatMap(_.as[Container]) match {
            case Right(container) => ContainerStarted(container)
            case Left(thr)        => ContainersPage.Error(thr.getMessage)
          },
        error => ContainersPage.Error(error.toString)
      )
    )
  }

  private def removeContainer(containerID: ContainerID): Cmd[IO, Message] = {
    Http.send(
      Request(method = Delete, url = s"$backendHost/docker/container/${containerID.value}"),
      Decoder[Message](
        response =>
          parse(response.body).flatMap(_.as[List[Container]]) match {
            case Right(containers) => ContainerRemoved(containers)
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

  case class AttemptStopContainer(containerID: ContainerID) extends Message

  case class ContainerStopped(container: Container) extends Message

  case class AttemptStartContainer(containerID: ContainerID) extends Message

  case class ContainerStarted(container: Container) extends Message

  case class AttemptRemoveContainer(containerID: ContainerID) extends Message

  case class ContainerRemoved(remainingContainers: List[Container]) extends Message

  case class Model(containers: List[Container])
}
