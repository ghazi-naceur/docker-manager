package in.oss.docker.manager.domain

case class IMAGE(value: String)
object IMAGE {
  val fieldName                              = "IMAGE"
  def getIndex(terminalLogLine: String): Int = terminalLogLine.indexOf(fieldName)

  def apply(headerLogLine: String, terminalLogLine: String): IMAGE =
    IMAGE(terminalLogLine.substring(getIndex(headerLogLine), COMMAND.getIndex(headerLogLine)).trim)
}
