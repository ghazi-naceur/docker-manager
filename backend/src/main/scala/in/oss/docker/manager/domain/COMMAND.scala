package in.oss.docker.manager.domain

case class COMMAND(value: String)
object COMMAND {
  val fieldName                              = "COMMAND"
  def getIndex(terminalLogLine: String): Int = terminalLogLine.indexOf(fieldName)

  def apply(headerLogLine: String, terminalLogLine: String): COMMAND =
    COMMAND(terminalLogLine.substring(getIndex(headerLogLine), CREATED.getIndex(headerLogLine)).trim)
}
