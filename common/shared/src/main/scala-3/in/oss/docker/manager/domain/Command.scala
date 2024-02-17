package in.oss.docker.manager.domain

case class Command(value: String)

object Command {

  val fieldName = "COMMAND"

  def getIndex(terminalLogLine: String): Int = terminalLogLine.indexOf(fieldName)

  def apply(headerLogLine: String, terminalLogLine: String, commandEndIndex: Int): Command =
    Command(terminalLogLine.substring(getIndex(headerLogLine), commandEndIndex).trim)
}
