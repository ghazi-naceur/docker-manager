package in.oss.docker.manager.domain

case class NAMES(value: String)
object NAMES {
  val fieldName                              = "NAMES"
  def getIndex(terminalLogLine: String): Int = terminalLogLine.indexOf(fieldName)

  def apply(headerLogLine: String, terminalLogLine: String): NAMES =
    NAMES(terminalLogLine.substring(getIndex(headerLogLine), SIZE.getIndex(headerLogLine)).trim)
}
