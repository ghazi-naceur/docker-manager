package in.oss.docker.manager.pages

import cats.effect.IO
import in.oss.docker.manager.pages
import in.oss.docker.manager.domain.Image
import in.oss.docker.manager.pages.ImagesPage.{LoadImages, Message}
import tyrian.{Cmd, Html}
import io.circe.parser.*
import io.circe.generic.auto.*
import tyrian.*
import tyrian.Html.*
import tyrian.http.*

final case class ImagesPage(backendHost: String, currentPage: Int = 1, itemsPerPage: Int = 10, images: List[Image] = List())
    extends Page {

  override def initCmd: Cmd[IO, Page.Message] = getImagesEndpoint

  override def update(message: Page.Message): (Page, Cmd[IO, Page.Message]) = message match {
    case ImagesPage.LoadImages(img)      => (this.copy(images = images ++ img), Cmd.None)
    case ImagesPage.GoToPage(pageNumber) => (this.copy(currentPage = pageNumber), Cmd.None)
    case ImagesPage.NoOp                 => (this, Cmd.None)
    case ImagesPage.Error(error)         => (this, Cmd.None)
  }

  override def view(): Html[Page.Message] = {
    val totalPages = (this.images.length.toDouble / this.itemsPerPage).ceil.toInt
    val startIndex = (this.currentPage - 1) * this.itemsPerPage
    val endIndex   = startIndex + this.itemsPerPage
    val pageItems  = this.images.slice(startIndex, endIndex)

    div(`class` := "col-md-8")(
      div(h2("Images list")),
      table(`class` := "table")(
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
          for (image <- pageItems)
            yield tr(
              td(`class` := "align-middle")(image.repository.value),
              td(`class` := "align-middle")(image.tag.value),
              td(`class` := "align-middle")(image.imageId.value),
              td(`class` := "align-middle")(image.created.value),
              td(`class` := "align-middle")(image.size.value)
            )
        )
      ),
      div(cls := "d-flex justify-content-center mt-3")(
        (for (page <- 1 to totalPages) yield {
          button(
            cls := "btn btn-secondary mx-1" + (if (page == this.currentPage) " active" else ""),
            onClick(ImagesPage.GoToPage(page))
          )(page.toString)
        }).toList
      )
    )
  }

  private def getImagesEndpoint: Cmd[IO, Message] = {
    Http.send(
      Request.get(s"$backendHost/docker/images"),
      Decoder[Message](
        response =>
          parse(response.body).flatMap(_.as[List[Image]]) match {
            case Right(images) => LoadImages(images)
            case Left(thr)     => ImagesPage.Error(thr.getMessage)
          },
        error => ImagesPage.Error(error.toString)
      )
    )
  }
}

object ImagesPage {
  trait Message extends pages.Page.Message

  case object NoOp extends Message

  case class LoadImages(images: List[Image]) extends Message

  case class Error(error: String) extends Message

  case class GoToPage(pageNumber: Int) extends Message

  case class Model(images: List[Image])
}
