package in.oss.docker.manager.domain

import io.circe.{Decoder, Encoder}

case class Ports(value: String)

object Ports {

  val fieldName = "PORTS"

  given Encoder[Ports] = Encoder.encodeString.contramap(_.value)
  given Decoder[Ports] = Decoder.decodeString.map(Ports(_))

  def getIndex(terminalLogLine: String): Int = terminalLogLine.indexOf(fieldName)

  def apply(headerLogLine: String, terminalLogLine: String, portsEndIndex: Int): Ports =
    Ports(terminalLogLine.substring(getIndex(headerLogLine), portsEndIndex).trim)
}
