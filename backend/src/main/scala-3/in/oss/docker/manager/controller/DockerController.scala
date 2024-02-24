package in.oss.docker.manager.controller

import cats.*
import cats.effect.*
import cats.implicits.*
import in.oss.docker.manager.cli.DockerCLI
import in.oss.docker.manager.errors.DockerShellError.DockerFailure
import io.circe.generic.auto.*
import org.http4s.circe.CirceEntityCodec.*
import org.http4s.*
import org.http4s.dsl.*
import org.http4s.server.Router

class DockerController[F[_]: Async](dockerCLI: DockerCLI[F]) extends Http4sDsl[F] {

  val getDockerContainers: HttpRoutes[F] = HttpRoutes.of[F] { case GET -> Root / "containers" =>
    dockerCLI.getContainers
      .flatMap(Ok(_))
      .recoverWith { case thr =>
        InternalServerError(DockerFailure(thr.getMessage))
      }
  }

  val getDockerImages: HttpRoutes[F] = HttpRoutes.of[F] { case GET -> Root / "images" =>
    dockerCLI.getImages
      .flatMap(Ok(_))
      .recoverWith { case thr =>
        InternalServerError(DockerFailure(thr.getMessage))
      }
  }

  val stopDockerContainer: HttpRoutes[F] = HttpRoutes.of[F] { case PUT -> Root / "container" / containerID / "stop" =>
    dockerCLI
      .stopContainer(containerID)
      .flatMap(Ok(_))
      .recoverWith { case thr =>
        InternalServerError(DockerFailure(thr.getMessage))
      }
  }

  val startDockerContainer: HttpRoutes[F] = HttpRoutes.of[F] { case PUT -> Root / "container" / containerID / "start" =>
    dockerCLI
      .startContainer(containerID)
      .flatMap(Ok(_))
      .recoverWith { case thr =>
        InternalServerError(DockerFailure(thr.getMessage))
      }
  }

  val removeDockerContainer: HttpRoutes[F] = HttpRoutes.of[F] { case DELETE -> Root / "container" / containerID =>
    dockerCLI
      .removeContainer(containerID)
      .flatMap(Ok(_))
      .recoverWith { case thr =>
        InternalServerError(DockerFailure(thr.getMessage))
      }
  }

  def routes: HttpApp[F] = Router(
    "/docker" -> (getDockerContainers <+> getDockerImages <+> stopDockerContainer <+> startDockerContainer <+> removeDockerContainer)
  ).orNotFound
}
