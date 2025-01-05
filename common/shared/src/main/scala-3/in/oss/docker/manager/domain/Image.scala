package in.oss.docker.manager.domain

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

case class Image(
    repository: Repository,
    tag: Tag,
    imageId: ImageID,
    created: Created,
    size: Size
)

object Image {

  given Encoder[Image] = deriveEncoder
  given Decoder[Image] = deriveDecoder

  val imageFields: List[String] = List(
    Repository.fieldName,
    Tag.fieldName,
    ImageID.fieldName,
    Created.fieldName,
    Size.fieldName
  )
}
