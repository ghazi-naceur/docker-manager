package in.oss.docker.manager.command

import in.oss.docker.manager.command.DockerPS.Result
import in.oss.docker.manager.domain.*

import scala.sys.process.*

case class DockerPS(results: List[Result])
object DockerPS {

  case class Result(
      containerId: CONTAINER_ID,
      image: IMAGE,
      command: COMMAND,
      created: CREATED,
      status: STATUS,
      ports: PORTS,
      names: NAMES
  )

  def execute: List[Result] = {
    val commandOutput = Seq("docker", "ps").!!
    val logLines      = commandOutput.split("\\n").toList
    val headerLogLine = logLines.head
    logLines.tail.map(line =>
      Result(
        CONTAINER_ID(headerLogLine, line),
        IMAGE(headerLogLine, line),
        COMMAND(headerLogLine, line),
        CREATED(headerLogLine, line),
        STATUS(headerLogLine, line),
        PORTS(headerLogLine, line),
        NAMES(headerLogLine, line)
      )
    )
  }
}
