package in.oss.docker.manager.shell

import cats.effect.Async
import in.oss.docker.manager.domain.*
import cats.implicits.*
import org.typelevel.log4cats.Logger

import scala.sys.process.*

trait DockerShell[F[_]] {
  def getContainers: F[List[Container]]
  def getImages: F[List[Image]]
  def stopContainer(containerID: String): F[Unit]
}

object DockerShell {

  def impl[F[_]: Logger: Async](): DockerShell[F] = new DockerShell[F] {

    override def getContainers: F[List[Container]] =
      for {
        _             <- Logger[F].info("Listing docker containers: 'docker ps -a -s'")
        commandOutput <- Async[F].delay(Seq("docker", "ps", "-a", "-s").!!)
        result        <- extractGetContainersResult(commandOutput)
      } yield result

    override def getImages: F[List[Image]] =
      for {
        _             <- Logger[F].info("Listing docker images '$a:' 'docker images -a'")
        commandOutput <- Async[F].delay(Seq("docker", "images", "-a").!!)
        result        <- extractGetImagesResult(commandOutput)
      } yield result

    override def stopContainer(containerID: String): F[Unit] =
      for {
        _             <- Logger[F].info(s"Stopping container '$containerID': 'docker stop $containerID'")
        commandOutput <- Async[F].delay(Seq("docker", "stop", s"$containerID").!!)
        result        <- extractStopContainerResult(containerID, commandOutput)
      } yield result
  }

  private def extractGetContainersResult[F[_]: Logger: Async](commandOutput: String) = {
    val logLines      = commandOutput.split("\\n").toList
    val headerLogLine = logLines.head

    if (checkFieldsExistence(headerLogLine, Container.containerFields))
      logLines.tail
        .map(line =>
          Container(
            ContainerID(headerLogLine, line, ImageName.getIndex(headerLogLine)),
            ImageName(headerLogLine, line, Command.getIndex(headerLogLine)),
            Command(headerLogLine, line, Created.getIndex(headerLogLine)),
            Created(headerLogLine, line, Status.getIndex(headerLogLine)),
            Status(headerLogLine, line, Ports.getIndex(headerLogLine)),
            Ports(headerLogLine, line, Names.getIndex(headerLogLine)),
            Names(headerLogLine, line, Size.getIndex(headerLogLine)),
            Size(headerLogLine, line, line.length)
          )
        )
        .pure
    else new Throwable(commandOutput).raiseError
  }

  private def extractGetImagesResult[F[_]: Async](commandOutput: String) = {
    val logLines      = commandOutput.split("\\n").toList
    val headerLogLine = logLines.head
    if (checkFieldsExistence(headerLogLine, Image.imageFields))
      logLines.tail
        .map(line =>
          Image(
            Repository(headerLogLine, line, Tag.getIndex(headerLogLine)),
            Tag(headerLogLine, line, ImageID.getIndex(headerLogLine)),
            ImageID(headerLogLine, line, Created.getIndex(headerLogLine)),
            Created(headerLogLine, line, Size.getIndex(headerLogLine)),
            Size(headerLogLine, line, line.length)
          )
        )
        .pure
    else new Throwable(commandOutput).raiseError
  }

  private def extractStopContainerResult[F[_]: Async](containerID: String, commandOutput: String) = {
    if (commandOutput.replace("\n", "") == containerID) ().pure
    else new Throwable(commandOutput).raiseError
  }

  private def checkFieldsExistence(header: String, fields: List[String]): Boolean =
    fields.map(header.contains).reduce(_ && _)
}
