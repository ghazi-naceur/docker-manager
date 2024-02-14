package in.oss.docker.manager.shell

import in.oss.docker.manager.domain.*
import cats.implicits.*
import scala.sys.process.*

object DockerShell {

  def getContainers: Either[String, List[Container]] = {
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
        .asRight
    else Left(commandOutput)
  }

  def getImages: Either[String, List[Image]] = {
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
        .asRight
    else Left(commandOutput)
  }

  def stopContainer(containerID: String): Either[String, Unit] = {
    println(s"Stopping container: 'docker stop $containerID'...")
    val commandOutput = Seq("docker", "stop", s"$containerID").!!
    if (commandOutput.replace("\n", "") == containerID) Right(())
    else Left(commandOutput)
  }

  private def checkFieldsExistence(header: String, fields: List[String]): Boolean =
    fields.map(header.contains).reduce(_ && _)
}
