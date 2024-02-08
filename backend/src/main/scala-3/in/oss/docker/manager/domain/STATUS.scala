package in.oss.docker.manager.domain

case class STATUS(value: String)
object STATUS {
  val fieldName                              = "STATUS"
  def getIndex(terminalLogLine: String): Int = terminalLogLine.indexOf(fieldName)

  def apply(headerLogLine: String, terminalLogLine: String): STATUS =
    STATUS(terminalLogLine.substring(getIndex(headerLogLine), PORTS.getIndex(headerLogLine)).trim)
}
