package in.oss.docker.manager

import in.oss.docker.manager.command.DockerPS

object Main {

  def main(args: Array[String]): Unit = {
    val result = DockerPS.execute
    println(result)
  }
}
