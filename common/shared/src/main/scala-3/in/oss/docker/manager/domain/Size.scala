package in.oss.docker.manager.domain

import io.circe.{Decoder, Encoder}

case class Size(value: String)

object Size {

  val fieldName = "SIZE"

  given Encoder[Size] = Encoder.encodeString.contramap(_.value)
  given Decoder[Size] = Decoder.decodeString.map(Size(_))

  def getIndex(terminalLogLine: String): Int = terminalLogLine.indexOf(fieldName)

  def apply(headerLogLine: String, terminalLogLine: String, sizeEndIndex: Int): Size =
    Size(terminalLogLine.substring(getIndex(headerLogLine), sizeEndIndex).trim)
}
