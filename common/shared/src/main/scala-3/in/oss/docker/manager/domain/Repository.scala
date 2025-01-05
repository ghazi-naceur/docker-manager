package in.oss.docker.manager.domain

import io.circe.{Decoder, Encoder}

case class Repository(value: String)

object Repository {

  val fieldName = "REPOSITORY"

  given Encoder[Repository] = Encoder.encodeString.contramap(_.value)
  given Decoder[Repository] = Decoder.decodeString.map(Repository(_))

  def getIndex(terminalLogLine: String): Int = terminalLogLine.indexOf(fieldName)

  def apply(headerLogLine: String, terminalLogLine: String, repositoryEndIndex: Int): Repository =
    Repository(terminalLogLine.substring(getIndex(headerLogLine), repositoryEndIndex).trim)
}
