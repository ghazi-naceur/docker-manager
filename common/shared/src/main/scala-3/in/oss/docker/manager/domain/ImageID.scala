package in.oss.docker.manager.domain

import io.circe.{Decoder, Encoder}

case class ImageID(value: String)

object ImageID {

  val fieldName = "IMAGE ID"

  given Encoder[ImageID] = Encoder.encodeString.contramap(_.value)
  given Decoder[ImageID] = Decoder.decodeString.map(ImageID(_))

  def getIndex(terminalLogLine: String): Int = terminalLogLine.indexOf(fieldName)

  def apply(headerLogLine: String, terminalLogLine: String, imageIdEndIndex: Int): ImageID =
    ImageID(terminalLogLine.substring(getIndex(headerLogLine), imageIdEndIndex).trim)
}
