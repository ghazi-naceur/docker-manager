package in.oss.docker.manager.shell

import in.oss.docker.manager.domain.*

import scala.sys.process.*

object DockerShell {
  def getContainers: List[Container] = {
    println("Executing command 'docker ps -a -s'...")
    val commandOutput = Seq("docker", "ps", "-a", "-s").!!
    val logLines      = commandOutput.split("\\n").toList
    val headerLogLine = logLines.head
    logLines.tail.map(line =>
      Container(
        CONTAINER_ID(headerLogLine, line),
        IMAGE(headerLogLine, line),
        COMMAND(headerLogLine, line),
        CREATED(headerLogLine, line),
        STATUS(headerLogLine, line),
        PORTS(headerLogLine, line),
        NAMES(headerLogLine, line),
        SIZE(headerLogLine, line)
      )
    )
  }
}
