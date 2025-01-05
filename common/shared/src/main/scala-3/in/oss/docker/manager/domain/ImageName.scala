package in.oss.docker.manager.domain

import io.circe.{Decoder, Encoder}

case class ImageName(value: String)

object ImageName {

  val fieldName = "IMAGE"

  given Encoder[ImageName] = Encoder.encodeString.contramap(_.value)
  given Decoder[ImageName] = Decoder.decodeString.map(ImageName(_))

  def getIndex(terminalLogLine: String): Int = terminalLogLine.indexOf(fieldName)

  def apply(headerLogLine: String, terminalLogLine: String, imageNameEndIndex: Int): ImageName =
    ImageName(terminalLogLine.substring(getIndex(headerLogLine), imageNameEndIndex).trim)
}
