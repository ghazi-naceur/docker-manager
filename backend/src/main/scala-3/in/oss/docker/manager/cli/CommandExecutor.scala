package in.oss.docker.manager.cli

import cats.effect.Async
import cats.*
import cats.implicits.*
import org.typelevel.log4cats.Logger

import scala.sys.process.*
import scala.util.{Failure, Success, Try}

trait CommandExecutor[F[_]] {

  def execute(command: Seq[String]): F[String]
}

object CommandExecutor {

  case class CommandError(message: String) extends Throwable(message)

  def impl[F[_]: Logger: Async]: CommandExecutor[F] = new CommandExecutor[F] {

    val outputBuffer: StringBuilder = new StringBuilder
    val errorBuffer: StringBuilder  = new StringBuilder

    val processLogger: ProcessLogger = ProcessLogger(
      out => outputBuffer.append(out),
      err => errorBuffer.append(err)
    )

    override def execute(command: Seq[String]): F[String] =
      Try {
        Process(command).!(processLogger)
      } match {
        case Success(exitCode) =>
          val (output, error) = collectLogs
          exitCode match {
            case 0 => output.pure[F]
            case _ => CommandError(error).raiseError
          }
        case Failure(exception) => CommandError(exception.getMessage).raiseError
      }

    def collectLogs: (String, String) = {
      val (output, error) = (outputBuffer.toString, errorBuffer.toString)
      outputBuffer.clear
      errorBuffer.clear
      (output, error)
    }
  }
}
