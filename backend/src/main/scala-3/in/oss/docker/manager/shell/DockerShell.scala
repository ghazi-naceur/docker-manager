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
        ContainerID(headerLogLine, line),
        ImageName(headerLogLine, line),
        Command(headerLogLine, line),
        Created(headerLogLine, line),
        Status(headerLogLine, line),
        Ports(headerLogLine, line),
        Names(headerLogLine, line),
        Size(headerLogLine, line)
      )
    )
  }
}
