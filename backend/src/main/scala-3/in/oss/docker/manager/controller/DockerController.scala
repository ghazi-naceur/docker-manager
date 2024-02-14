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

class DockerController[F[_]: Async](dockerShell: DockerShell[F]) extends Http4sDsl[F] {

  val getDockerContainers: HttpRoutes[F] = HttpRoutes.of[F] { case GET -> Root / "containers" =>
    dockerShell.getContainers
      .flatMap(Ok(_))
      .recoverWith { case thr =>
        InternalServerError(thr.getMessage)
      }
  }

  val getDockerImages: HttpRoutes[F] = HttpRoutes.of[F] { case GET -> Root / "images" =>
    dockerShell.getImages
      .flatMap(Ok(_))
      .recoverWith { case thr =>
        InternalServerError(thr.getMessage)
      }
  }

  val stopDockerContainer: HttpRoutes[F] = HttpRoutes.of[F] { case PUT -> Root / "container" / containerID =>
    dockerShell
      .stopContainer(containerID)
      .flatMap(Ok(_))
      .recoverWith { case thr =>
        InternalServerError(thr.getMessage)
      }
  }

  def routes: HttpApp[F] = Router(
    "/docker" -> (getDockerContainers <+> getDockerImages <+> stopDockerContainer)
  ).orNotFound
}
