package in.oss.docker.manager.cli

import cats.effect.Async
import in.oss.docker.manager.domain.*
import cats.implicits.*
import in.oss.docker.manager.errors.DockerShellError.{
  ContainerStatusError,
  GetContainersError,
  GetImagesError,
  IrremovableContainer,
  UnavailableContainer
}
import org.typelevel.log4cats.Logger

trait DockerCLI[F[_]] {
  def getContainers: F[List[Container]]
  def getImages: F[List[Image]]
  def stopContainer(containerID: String): F[Container]
  def startContainer(containerID: String): F[Container]
  def removeContainer(containerID: String): F[List[Container]]
}

object DockerCLI {

  def impl[F[_]: Logger: Async](commandExecutor: CommandExecutor[F]): DockerCLI[F] = new DockerCLI[F] {

    override def getContainers: F[List[Container]] =
      for {
        _             <- Logger[F].info("Listing all docker containers: 'docker ps -a -s'")
        commandOutput <- commandExecutor.execute(Seq("docker", "ps", "-a", "-s"))
        _             <- Logger[F].info(s"com: $commandOutput")
        result        <- extractGetContainersResult(commandOutput)
      } yield result

    override def getImages: F[List[Image]] =
      for {
        _             <- Logger[F].info("Listing all docker images: 'docker images -a'")
        commandOutput <- commandExecutor.execute(Seq("docker", "images", "-a"))
        result        <- extractGetImagesResult(commandOutput)
      } yield result

    override def stopContainer(containerID: String): F[Container] =
      for {
        _                <- Logger[F].info(s"Stopping container '$containerID': 'docker stop $containerID'")
        commandOutput    <- commandExecutor.execute(Seq("docker", "stop", containerID))
        _                <- checkContainerStatusResult(containerID, commandOutput)
        containers       <- getContainers
        stoppedContainer <- getContainer(containerID, containers)
      } yield stoppedContainer

    override def startContainer(containerID: String): F[Container] =
      for {
        _                <- Logger[F].info(s"Starting container '$containerID': 'docker start $containerID'")
        commandOutput    <- commandExecutor.execute(Seq("docker", "start", containerID))
        _                <- checkContainerStatusResult(containerID, commandOutput)
        containers       <- getContainers
        startedContainer <- getContainer(containerID, containers)
      } yield startedContainer

    override def removeContainer(containerID: String): F[List[Container]] =
      (for {
        _             <- Logger[F].info(s"Removing container '$containerID': 'docker rm $containerID'")
        commandOutput <- commandExecutor.execute(Seq("docker", "rm", containerID))
        _             <- checkContainerStatusResult(containerID, commandOutput)
        containers    <- getContainers
      } yield containers).adaptError {
        case thr if thr.getMessage.contains("Stop the container before attempting removal or force remove") =>
          IrremovableContainer(containerID)
      }
  }

  def getContainer[F[_]: Async](containerID: String, containers: List[Container]): F[Container] = {
    containers.find(_.containerId.value == containerID) match {
      case Some(container) => container.pure[F]
      case None            => UnavailableContainer(containerID).raiseError
    }
  }

  def extractGetContainersResult[F[_]: Async](commandOutput: String): F[List[Container]] = {
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
    else GetContainersError(headerLogLine, Container.containerFields).raiseError
  }

  def extractGetImagesResult[F[_]: Async](commandOutput: String): F[List[Image]] = {
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
    else GetImagesError(headerLogLine, Image.imageFields).raiseError
  }

  def checkContainerStatusResult[F[_]: Async](containerID: String, commandOutput: String): F[Unit] = {
    if (commandOutput.replace("\n", "") == containerID) ().pure
    else ContainerStatusError(containerID, commandOutput).raiseError
  }

  private def checkFieldsExistence(header: String, fields: List[String]): Boolean =
    fields.map(header.contains).reduce(_ && _)
}
