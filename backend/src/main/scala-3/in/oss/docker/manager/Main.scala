package in.oss.docker.manager

import cats.effect.{IO, IOApp}
import in.oss.docker.manager.config.AppConfig
import in.oss.docker.manager.controller.DockerController
import org.http4s.ember.server.EmberServerBuilder
import pureconfig.ConfigSource
import in.oss.docker.manager.config.Syntax.*
import in.oss.docker.manager.shell.DockerShell
import org.http4s.server.middleware.CORS

object Main extends IOApp.Simple {

  override def run: IO[Unit] = {
    ConfigSource.default
      .loadF[IO, AppConfig]
      .flatMap { case AppConfig(emberServerConfig) =>
        val dockerShell = DockerShell.impl[IO]()
        EmberServerBuilder
          .default[IO]
          .withHost(emberServerConfig.host)
          .withPort(emberServerConfig.port)
          .withHttpApp(CORS.policy(DockerController[IO](dockerShell).routes))
          .build
          .use(_ => IO.println("Docker Manager started...") *> IO.never)
      }
  }
}
