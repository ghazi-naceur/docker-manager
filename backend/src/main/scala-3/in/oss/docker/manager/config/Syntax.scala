package in.oss.docker.manager.config

import cats.MonadThrow
import cats.implicits.*
import pureconfig.error.ConfigReaderException
import pureconfig.{ConfigReader, ConfigSource}

import scala.reflect.ClassTag

object Syntax {

  extension (source: ConfigSource)
    def loadF[F[_], A](using reader: ConfigReader[A], F: MonadThrow[F], tag: ClassTag[A]): F[A] =
      F.pure(source.load[A])
        .flatMap {
          case Right(value) => F.pure(value)
          case Left(errors) => F.raiseError[A](ConfigReaderException(errors))
        }
}
