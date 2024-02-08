package in.oss.docker.manager.domain

case class CONTAINER_ID private (value: String)
object CONTAINER_ID {
  val fieldName                              = "CONTAINER ID"
  def getIndex(terminalLogLine: String): Int = terminalLogLine.indexOf(fieldName)
  def apply(headerLogLine: String, terminalLogLine: String): CONTAINER_ID =
    CONTAINER_ID(terminalLogLine.substring(getIndex(headerLogLine), IMAGE.getIndex(headerLogLine)).trim)
}
