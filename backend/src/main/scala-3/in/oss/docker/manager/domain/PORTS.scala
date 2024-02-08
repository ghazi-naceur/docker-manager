package in.oss.docker.manager.domain

case class PORTS(value: String)
object PORTS {
  val fieldName                              = "PORTS"
  def getIndex(terminalLogLine: String): Int = terminalLogLine.indexOf(fieldName)

  def apply(headerLogLine: String, terminalLogLine: String): PORTS =
    PORTS(terminalLogLine.substring(getIndex(headerLogLine), NAMES.getIndex(headerLogLine)).trim)
}
