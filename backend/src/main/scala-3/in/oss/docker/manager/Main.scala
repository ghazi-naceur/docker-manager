package in.oss.docker.manager

import cats.effect.{IO, IOApp}
import com.comcast.ip4s.{host, port}
import in.oss.docker.manager.config.AppConfig
import in.oss.docker.manager.controller.DockerController
import org.http4s.ember.server.EmberServerBuilder
import pureconfig.ConfigSource
import in.oss.docker.manager.config.Syntax.*
import org.http4s.server.middleware.CORS

object Main extends IOApp.Simple {

  override def run: IO[Unit] = {
    ConfigSource.default
      .loadF[IO, AppConfig]
      .flatMap { case AppConfig(emberServerConfig) =>
        EmberServerBuilder
          .default[IO]
          .withHost(emberServerConfig.host)
          .withPort(emberServerConfig.port)
          .withHttpApp(CORS.policy(DockerController[IO]().routes))
          .build
          .use(_ => IO.println("Docker Manager started...") *> IO.never)
      }
  }
}
