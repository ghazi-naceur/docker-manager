package in.oss.docker.manager.domain

import io.circe.{Decoder, Encoder}

case class Command(value: String)

object Command {

  val fieldName = "COMMAND"

  given Encoder[Command] = Encoder.encodeString.contramap(_.value)
  given Decoder[Command] = Decoder.decodeString.map(Command(_))

  def getIndex(terminalLogLine: String): Int = terminalLogLine.indexOf(fieldName)

  def apply(headerLogLine: String, terminalLogLine: String, commandEndIndex: Int): Command =
    Command(terminalLogLine.substring(getIndex(headerLogLine), commandEndIndex).trim)
}
