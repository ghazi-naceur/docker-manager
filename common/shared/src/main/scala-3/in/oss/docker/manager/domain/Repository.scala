package in.oss.docker.manager.domain

case class Repository(value: String)

object Repository {

  val fieldName = "REPOSITORY"

  def getIndex(terminalLogLine: String): Int = terminalLogLine.indexOf(fieldName)

  def apply(headerLogLine: String, terminalLogLine: String, repositoryEndIndex: Int): Repository =
    Repository(terminalLogLine.substring(getIndex(headerLogLine), repositoryEndIndex).trim)
}
