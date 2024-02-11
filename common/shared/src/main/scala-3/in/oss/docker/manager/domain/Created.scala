package in.oss.docker.manager.domain

case class Created private (value: String)

object Created {

  val fieldName = "CREATED"

  def getIndex(terminalLogLine: String): Int = terminalLogLine.indexOf(fieldName)

  def apply(headerLogLine: String, terminalLogLine: String, createdEndIndex: Int): Created =
    Created(terminalLogLine.substring(getIndex(headerLogLine), createdEndIndex).trim)
}
