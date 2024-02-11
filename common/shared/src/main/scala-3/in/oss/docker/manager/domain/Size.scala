package in.oss.docker.manager.domain

case class Size private (value: String)

object Size {

  val fieldName = "SIZE"

  def getIndex(terminalLogLine: String): Int = terminalLogLine.indexOf(fieldName)

  def apply(headerLogLine: String, terminalLogLine: String): Size =
    Size(terminalLogLine.substring(getIndex(headerLogLine), terminalLogLine.length).trim)
}
