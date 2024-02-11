package in.oss.docker.manager.domain

case class Names private (value: String)

object Names {

  val fieldName = "NAMES"

  def getIndex(terminalLogLine: String): Int = terminalLogLine.indexOf(fieldName)

  def apply(headerLogLine: String, terminalLogLine: String, namesEndIndex: Int): Names =
    Names(terminalLogLine.substring(getIndex(headerLogLine), namesEndIndex).trim)
}
