package in.oss.docker.manager.domain

case class Ports private (value: String)

object Ports {

  val fieldName = "PORTS"

  def getIndex(terminalLogLine: String): Int = terminalLogLine.indexOf(fieldName)

  def apply(headerLogLine: String, terminalLogLine: String): Ports =
    Ports(terminalLogLine.substring(getIndex(headerLogLine), Names.getIndex(headerLogLine)).trim)
}
