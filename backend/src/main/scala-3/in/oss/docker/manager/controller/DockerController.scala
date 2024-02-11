package in.oss.docker.manager.controller

import cats.*
import cats.effect.*
import cats.implicits.*
import in.oss.docker.manager.shell.DockerShell
import io.circe.*
import io.circe.generic.auto.*
import org.http4s.*
import org.http4s.dsl.*
import org.http4s.circe.CirceEntityCodec.*
import org.http4s.dsl.Http4sDsl
import org.http4s.server.Router

class DockerController[F[_]: Async] extends Http4sDsl[F] {

  val getDockerContainers: HttpRoutes[F] = HttpRoutes.of[F] { case GET -> Root / "containers" =>
    Ok(DockerShell.getContainers)
  }

  val getDockerImages: HttpRoutes[F] = HttpRoutes.of[F] { case GET -> Root / "images" =>
    Ok(DockerShell.getImages)
  }

  def routes: HttpApp[F] = Router(
    "/docker" -> (getDockerContainers <+> getDockerImages)
  ).orNotFound
}
