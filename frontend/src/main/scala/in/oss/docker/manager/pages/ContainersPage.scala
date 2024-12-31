package in.oss.docker.manager.pages

import com.raquo.airstream.ownership.OneTimeOwner
import com.raquo.laminar.api.L.{*, given}
import com.raquo.laminar.nodes.ReactiveHtmlElement
import in.oss.docker.manager.domain.*
import org.scalajs.dom
import org.scalajs.dom.{Fetch, HTMLDivElement, console}

import scala.concurrent.ExecutionContext.Implicits.global
import io.circe.parser.*
import io.circe.generic.auto.*

import scala.scalajs.js

object ContainersPage {

  def apply(): ReactiveHtmlElement[HTMLDivElement] = {
    val containersVar = Var(List.empty[Container])

    def fetchContainers(): Unit = {
      Fetch
        .fetch("http://localhost:6543/docker/containers")
        .toFuture
        .flatMap(_.text().toFuture)
        .foreach { responseText =>
          parse(responseText).flatMap(_.as[List[Container]]) match {
            case Right(containers) => containersVar.set(containers)
            case Left(thr)         => console.error("Error parsing JSON:", thr.getMessage)
          }
        }
    }

    def handleStopContainer(containerId: String): Unit = {
      AjaxStream
        .put(s"http://localhost:6543/docker/container/$containerId/stop")
        .foreach { xhr =>
          parse(xhr.responseText).flatMap(_.as[Container]) match {
            case Right(container) =>
              console.info(s"Container $containerId stopped")
              fetchContainers()
            case Left(thr) => console.error("Error parsing JSON:", thr.getMessage)
          }
        }(new OneTimeOwner(() => ()))
    }

    def handleStartContainer(containerId: String): Unit = {
      AjaxStream
        .put(s"http://localhost:6543/docker/container/$containerId/start")
        .foreach { xhr =>
          parse(xhr.responseText).flatMap(_.as[Container]) match {
            case Right(container) =>
              console.info(s"Container $containerId started")
              fetchContainers()
            case Left(thr) => console.error("Error parsing JSON:", thr.getMessage)
          }
        }(new OneTimeOwner(() => ()))
    }

    def handleRemoveContainer(containerId: String): Unit = {
      AjaxStream
        .delete(s"http://localhost:6543/docker/container/$containerId")
        .foreach { xhr =>
          parse(xhr.responseText).flatMap(_.as[List[Container]]) match {
            case Right(containers) =>
              console.info(s"Container $containerId removed")
              containersVar.set(containers)
            case Left(thr) => console.error("Error parsing JSON:", thr.getMessage)
          }
        }(new OneTimeOwner(() => ()))
    }

    // Call fetchContainers when the page is loaded
    fetchContainers()

    div(
      div(
        cls := "col-md-11",
        div(h2("Containers list")),
        table(
          cls := "table",
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
            children <-- containersVar.signal.map { containers =>
              containers.map { container =>
                tr(
                  td(cls := "align-middle", container.containerId.value),
                  td(cls := "align-middle", container.imageName.value),
                  td(cls := "align-middle", container.command.value),
                  td(cls := "align-middle", container.created.value),
                  td(cls := "align-middle", container.status.value),
                  td(cls := "align-middle", container.ports.value),
                  td(cls := "align-middle", container.names.value),
                  td(cls := "align-middle", container.size.value),
                  td(
                    cls := "align-middle",
                    div(
                      cls := "dropdown",
                      button(
                        cls                   := "btn btn-secondary dropdown-toggle",
                        tpe                   := "button",
                        idAttr                := "dropdownMenuButton",
                        dataAttr("bs-toggle") := "dropdown",
                        aria.expanded         := false,
                        "Choose action"
                      ),
                      div(
                        cls             := "dropdown-menu",
                        aria.labelledBy := "dropdownMenuButton",
                        a(
                          cls := "dropdown-item",
                          onClick --> (_ => handleStopContainer(container.containerId.value)),
                          "Stop"
                        ),
                        a(
                          cls := "dropdown-item",
                          onClick --> (_ => handleStartContainer(container.containerId.value)),
                          "Start"
                        ),
                        a(
                          cls := "dropdown-item",
                          onClick --> (_ => handleRemoveContainer(container.containerId.value)),
                          "Remove"
                        )
                      )
                    )
                  )
                )
              }
            }
          )
        )
      )
    )
  }
}
