package in.oss.docker.manager.controller

import cats.data.Kleisli
import cats.effect.Async
import in.oss.docker.manager.command.DockerPS
import io.circe.*
import io.circe.generic.auto.*
import org.http4s.*
import org.http4s.circe.CirceEntityCodec.*
import org.http4s.dsl.Http4sDsl
import org.http4s.server.Router

class DockerController[F[_]: Async] extends Http4sDsl[F] {

  val getDockerContainers: HttpRoutes[F] = HttpRoutes.of[F] { case GET -> Root / "containers" =>
    Ok(DockerPS.execute)
  }

  def routes: HttpApp[F] = Router(
    "/docker" -> getDockerContainers
  ).orNotFound
}
