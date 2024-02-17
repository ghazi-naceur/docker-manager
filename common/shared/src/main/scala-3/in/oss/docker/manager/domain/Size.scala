package in.oss.docker.manager.domain

case class Size(value: String)

object Size {

  val fieldName = "SIZE"

  def getIndex(terminalLogLine: String): Int = terminalLogLine.indexOf(fieldName)

  def apply(headerLogLine: String, terminalLogLine: String, sizeEndIndex: Int): Size =
    Size(terminalLogLine.substring(getIndex(headerLogLine), sizeEndIndex).trim)
}
