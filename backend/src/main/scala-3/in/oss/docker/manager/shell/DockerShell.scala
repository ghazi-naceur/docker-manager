package in.oss.docker.manager.shell

import cats.effect.Async
import in.oss.docker.manager.domain.*
import cats.implicits.*
import scala.sys.process.*

trait DockerShell[F[_]] {
  def getContainers: F[List[Container]]
  def getImages: F[List[Image]]
  def stopContainer(containerID: String): F[Unit]
}

object DockerShell {

  def impl[F[_]: Async](): DockerShell[F] = new DockerShell[F] {

    override def getContainers: F[List[Container]] = {
      println("Listing docker containers: 'docker ps -a -s'...")
      val commandOutput = Seq("docker", "ps", "-a", "-s").!!
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

    override def getImages: F[List[Image]] = {
      println("Listing docker images: 'docker images -a'...")
      val commandOutput = Seq("docker", "images", "-a").!!
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

    override def stopContainer(containerID: String): F[Unit] = {
      println(s"Stopping container: 'docker stop $containerID'...")
      val commandOutput = Seq("docker", "stop", s"$containerID").!!
      if (commandOutput.replace("\n", "") == containerID) ().pure
      else new Throwable(commandOutput).raiseError
    }
  }

  private def checkFieldsExistence(header: String, fields: List[String]): Boolean =
    fields.map(header.contains).reduce(_ && _)
}
