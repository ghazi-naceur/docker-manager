package in.oss.docker.manager.domain

import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}

case class Container(
    containerId: ContainerID,
    imageName: ImageName,
    command: Command,
    created: Created,
    status: Status,
    ports: Ports,
    names: Names,
    size: Size
)

object Container {

  given Encoder[Container] = deriveEncoder
  given Decoder[Container] = deriveDecoder

  val containerFields: List[String] = List(
    ContainerID.fieldName,
    ImageName.fieldName,
    Command.fieldName,
    Created.fieldName,
    Status.fieldName,
    Ports.fieldName,
    Names.fieldName,
    Size.fieldName
  )
}
