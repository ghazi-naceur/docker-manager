package in.oss.docker.manager.domain

case class ContainerID private (value: String)

object ContainerID {

  val fieldName = "CONTAINER ID"

  def getIndex(terminalLogLine: String): Int = terminalLogLine.indexOf(fieldName)
  def apply(headerLogLine: String, terminalLogLine: String): ContainerID =
    ContainerID(terminalLogLine.substring(getIndex(headerLogLine), ImageName.getIndex(headerLogLine)).trim)
}
