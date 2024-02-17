package in.oss.docker.manager.domain

case class ImageID(value: String)

object ImageID {

  val fieldName = "IMAGE ID"

  def getIndex(terminalLogLine: String): Int = terminalLogLine.indexOf(fieldName)

  def apply(headerLogLine: String, terminalLogLine: String, imageIdEndIndex: Int): ImageID =
    ImageID(terminalLogLine.substring(getIndex(headerLogLine), imageIdEndIndex).trim)
}
