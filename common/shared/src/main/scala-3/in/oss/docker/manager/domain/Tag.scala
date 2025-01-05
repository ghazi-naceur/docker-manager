package in.oss.docker.manager.domain

import io.circe.{Decoder, Encoder}

case class Tag(value: String)

object Tag {

  val fieldName = "TAG"

  given Encoder[Tag] = Encoder.encodeString.contramap(_.value)
  given Decoder[Tag] = Decoder.decodeString.map(Tag(_))

  def getIndex(terminalLogLine: String): Int = terminalLogLine.indexOf(fieldName)

  def apply(headerLogLine: String, terminalLogLine: String, tagEndIndex: Int): Tag =
    Tag(terminalLogLine.substring(getIndex(headerLogLine), tagEndIndex).trim)
}
