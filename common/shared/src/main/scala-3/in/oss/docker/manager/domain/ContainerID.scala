package in.oss.docker.manager.domain

case class ContainerID(value: String)

object ContainerID {

  val fieldName = "CONTAINER ID"

  def getIndex(terminalLogLine: String): Int = terminalLogLine.indexOf(fieldName)

  def apply(headerLogLine: String, terminalLogLine: String, containerIdEndIndex: Int): ContainerID =
    ContainerID(terminalLogLine.substring(getIndex(headerLogLine), containerIdEndIndex).trim)
}
