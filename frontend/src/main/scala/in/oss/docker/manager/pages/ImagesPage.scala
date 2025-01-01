package in.oss.docker.manager.pages

import com.raquo.airstream.ownership.OneTimeOwner
import com.raquo.laminar.api.L.{*, given}
import com.raquo.laminar.nodes.ReactiveHtmlElement
import in.oss.docker.manager.domain.Image as DomainImage
import io.circe.parser.*
import io.circe.generic.auto.*
import io.circe.parser.parse
import org.scalajs.dom.{HTMLDivElement, console}

object ImagesPage {

  def apply(): ReactiveHtmlElement[HTMLDivElement] = {
    val imagesVar = Var(List.empty[DomainImage])

    def fetchImages(): Unit = {
      AjaxStream
        .get("http://localhost:6543/docker/images")
        .foreach { xhr =>
          parse(xhr.responseText).flatMap(_.as[List[DomainImage]]) match {
            case Right(images) => imagesVar.set(images)
            case Left(thr)     => console.error("Error parsing JSON:", thr.getMessage)
          }
        }(new OneTimeOwner(() => ()))
    }

    fetchImages()

    div(
      div(h2("Images list")),
      table(
        cls := "table",
        thead(
          tr(
            th("Repository"),
            th("Tag"),
            th("Image ID"),
            th("Created"),
            th("Size")
          )
        ),
        tbody(
          children <-- imagesVar.signal.map { images =>
            images.map { image =>
              tr(
                td(cls := "align-middle", image.repository.value),
                td(cls := "align-middle", image.tag.value),
                td(cls := "align-middle", image.imageId.value),
                td(cls := "align-middle", image.created.value),
                td(cls := "align-middle", image.size.value)
              )
            }
          }
        )
      )
    )
  }
}
