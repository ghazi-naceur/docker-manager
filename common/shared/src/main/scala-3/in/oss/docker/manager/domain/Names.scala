package in.oss.docker.manager.domain

import io.circe.{Decoder, Encoder}

case class Names(value: String)

object Names {

  val fieldName = "NAMES"

  given Encoder[Names] = Encoder.encodeString.contramap(_.value)
  given Decoder[Names] = Decoder.decodeString.map(Names(_))

  def getIndex(terminalLogLine: String): Int = terminalLogLine.indexOf(fieldName)

  def apply(headerLogLine: String, terminalLogLine: String, namesEndIndex: Int): Names =
    Names(terminalLogLine.substring(getIndex(headerLogLine), namesEndIndex).trim)
}
