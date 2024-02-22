package in.oss.docker.manager

import cats.effect.{IO, IOApp}
import in.oss.docker.manager.config.AppConfig
import in.oss.docker.manager.controller.DockerController
import org.http4s.ember.server.EmberServerBuilder
import pureconfig.ConfigSource
import in.oss.docker.manager.config.Syntax.*
import in.oss.docker.manager.cli.{CommandExecutor, DockerCLI}
import org.http4s.server.middleware.CORS
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger

object Main extends IOApp.Simple {

  given logger: Logger[IO] = Slf4jLogger.getLogger[IO]

  override def run: IO[Unit] = {
    ConfigSource.default
      .loadF[IO, AppConfig]
      .flatMap { case AppConfig(emberServerConfig) =>
        val commandExecutor = CommandExecutor.impl[IO]
        val dockerShell     = DockerCLI.impl[IO](commandExecutor)
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
