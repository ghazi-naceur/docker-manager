package in.oss.docker.manager.domain

case class Tag(value: String)

object Tag {

  val fieldName = "TAG"

  def getIndex(terminalLogLine: String): Int = terminalLogLine.indexOf(fieldName)

  def apply(headerLogLine: String, terminalLogLine: String, tagEndIndex: Int): Tag =
    Tag(terminalLogLine.substring(getIndex(headerLogLine), tagEndIndex).trim)
}
