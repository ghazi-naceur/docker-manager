package in.oss.docker.manager.domain

import io.circe.{Decoder, Encoder}

case class Created(value: String)

object Created {

  val fieldName = "CREATED"

  given Encoder[Created] = Encoder.encodeString.contramap(_.value)
  given Decoder[Created] = Decoder.decodeString.map(Created(_))

  def getIndex(terminalLogLine: String): Int = terminalLogLine.indexOf(fieldName)

  def apply(headerLogLine: String, terminalLogLine: String, createdEndIndex: Int): Created =
    Created(terminalLogLine.substring(getIndex(headerLogLine), createdEndIndex).trim)
}
