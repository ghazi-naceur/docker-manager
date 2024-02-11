package in.oss.docker.manager.domain

case class SIZE(value: String)

object SIZE {
  val fieldName = "SIZE"

  def getIndex(terminalLogLine: String): Int = terminalLogLine.indexOf(fieldName)

  def apply(headerLogLine: String, terminalLogLine: String): SIZE =
    SIZE(terminalLogLine.substring(getIndex(headerLogLine), terminalLogLine.length).trim)
}
