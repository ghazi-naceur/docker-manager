package in.oss.docker.manager.domain

case class ImageName private (value: String)

object ImageName {

  val fieldName = "IMAGE"

  def getIndex(terminalLogLine: String): Int = terminalLogLine.indexOf(fieldName)

  def apply(headerLogLine: String, terminalLogLine: String): ImageName =
    ImageName(terminalLogLine.substring(getIndex(headerLogLine), Command.getIndex(headerLogLine)).trim)
}
