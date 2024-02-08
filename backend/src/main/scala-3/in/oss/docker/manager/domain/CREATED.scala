package in.oss.docker.manager.domain

case class CREATED(value: String)
object CREATED {
  val fieldName                              = "CREATED"
  def getIndex(terminalLogLine: String): Int = terminalLogLine.indexOf(fieldName)

  def apply(headerLogLine: String, terminalLogLine: String): CREATED =
    CREATED(terminalLogLine.substring(getIndex(headerLogLine), STATUS.getIndex(headerLogLine)).trim)
}
