package in.oss.docker.manager.domain

import io.circe.{Decoder, Encoder}

case class Status(value: String)

object Status {

  val fieldName = "STATUS"

  given Encoder[Status] = Encoder.encodeString.contramap(_.value)
  given Decoder[Status] = Decoder.decodeString.map(Status(_))

  def getIndex(terminalLogLine: String): Int = terminalLogLine.indexOf(fieldName)

  def apply(headerLogLine: String, terminalLogLine: String, statusEndIndex: Int): Status =
    Status(terminalLogLine.substring(getIndex(headerLogLine), statusEndIndex).trim)
}
