package in.oss.docker.manager.pages

import com.raquo.airstream.ownership.OneTimeOwner
import com.raquo.laminar.api.L.*
import com.raquo.laminar.nodes.ReactiveHtmlElement
import in.oss.docker.manager.components.Notification
import in.oss.docker.manager.domain.Image as DomainImage
import io.circe.parser.*
import io.circe.generic.auto.*
import io.circe.parser.parse
import org.scalajs.dom.{HTMLDivElement, console}

object ImagesPage {

  def apply(backendHost: String): ReactiveHtmlElement[HTMLDivElement] = {
    val imagesVar      = Var(List.empty[DomainImage])
    val currentPageVar = Var(1)
    val itemsPerPage   = 10

    def fetchImages(): Unit = {
      AjaxStream
        .get(s"$backendHost/docker/images")
        .recover { case thr: Throwable =>
          console.log(s"Failed to fetch images: ${thr.getMessage}")
          Notification.storeError(s"Failed to fetch images: ${thr.getMessage}")
          None
        }
        .foreach { xhr =>
          parse(xhr.responseText).flatMap(_.as[List[DomainImage]]) match {
            case Right(images) => imagesVar.set(images)
            case Left(thr)     => console.error("Error parsing JSON:", thr.getMessage)
          }
        }(new OneTimeOwner(() => ()))
    }

    fetchImages()

    def paginatedImages(images: List[DomainImage], page: Int, itemsPerPage: Int): List[DomainImage] = {
      val from = (page - 1) * itemsPerPage
      val to   = from + itemsPerPage
      images.slice(from, to)
    }

    def totalPages(images: List[DomainImage], itemsPerPage: Int): Int = {
      Math.ceil(images.size.toDouble / itemsPerPage).toInt
    }

    div(
      div(h2("Images list")),
      Notification.display(),
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
          children <-- imagesVar.signal.combineWith(currentPageVar.signal).map { case (images, currentPage) =>
            paginatedImages(images, currentPage, itemsPerPage).map { image =>
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
      ),
      div(
        cls := "pagination",
        ul(
          cls := "pagination",
          children <-- imagesVar.signal.combineWith(currentPageVar.signal).map { case (images, currentPage) =>
            val total = totalPages(images, itemsPerPage)
            (1 to total).map { page =>
              li(
                cls := s"page-item ${if (page == currentPage) "active" else ""}",
                a(
                  cls  := "page-link",
                  href := "#",
                  onClick.preventDefault.mapTo(page) --> currentPageVar.writer,
                  page.toString
                )
              )
            }
          }
        )
      )
    )
  }
}
