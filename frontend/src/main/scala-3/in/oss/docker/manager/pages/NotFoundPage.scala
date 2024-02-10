package in.oss.docker.manager.pages
import cats.effect.IO
import tyrian.Html.div
import tyrian.{Cmd, Html}

final case class NotFoundPage() extends Page {

  override def initCmd: Cmd[IO, Page.Message] = Cmd.None

  override def update(message: Page.Message): (Page, Cmd[IO, Page.Message]) = (this, Cmd.None)

  override def view(): Html[Page.Message] = div("Page not found")
}
