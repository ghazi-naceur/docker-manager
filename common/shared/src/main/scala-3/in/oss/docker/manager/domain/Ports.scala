package in.oss.docker.manager.domain

case class Ports(value: String)

object Ports {

  val fieldName = "PORTS"

  def getIndex(terminalLogLine: String): Int = terminalLogLine.indexOf(fieldName)

  def apply(headerLogLine: String, terminalLogLine: String, portsEndIndex: Int): Ports =
    Ports(terminalLogLine.substring(getIndex(headerLogLine), portsEndIndex).trim)
}
