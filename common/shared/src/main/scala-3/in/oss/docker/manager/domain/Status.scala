package in.oss.docker.manager.domain

case class Status(value: String)

object Status {

  val fieldName = "STATUS"

  def getIndex(terminalLogLine: String): Int = terminalLogLine.indexOf(fieldName)

  def apply(headerLogLine: String, terminalLogLine: String, statusEndIndex: Int): Status =
    Status(terminalLogLine.substring(getIndex(headerLogLine), statusEndIndex).trim)
}
