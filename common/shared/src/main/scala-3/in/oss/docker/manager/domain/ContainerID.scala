package in.oss.docker.manager.domain

import io.circe.{Decoder, Encoder}

case class ContainerID(value: String)

object ContainerID {

  val fieldName = "CONTAINER ID"

  given Encoder[ContainerID] = Encoder.encodeString.contramap(_.value)
  given Decoder[ContainerID] = Decoder.decodeString.map(ContainerID(_))

  def getIndex(terminalLogLine: String): Int = terminalLogLine.indexOf(fieldName)

  def apply(headerLogLine: String, terminalLogLine: String, containerIdEndIndex: Int): ContainerID =
    ContainerID(terminalLogLine.substring(getIndex(headerLogLine), containerIdEndIndex).trim)
}
