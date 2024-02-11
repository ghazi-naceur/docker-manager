package in.oss.docker.manager.domain

case class Command private (value: String)

object Command {

  val fieldName = "COMMAND"

  def getIndex(terminalLogLine: String): Int = terminalLogLine.indexOf(fieldName)

  def apply(headerLogLine: String, terminalLogLine: String): Command =
    Command(terminalLogLine.substring(getIndex(headerLogLine), Created.getIndex(headerLogLine)).trim)
}
