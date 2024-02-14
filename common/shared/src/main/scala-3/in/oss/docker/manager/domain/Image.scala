package in.oss.docker.manager.domain

case class Image(
    repository: Repository,
    tag: Tag,
    imageId: ImageID,
    created: Created,
    size: Size
)

object Image {
  val imageFields: List[String] = List(
    Repository.fieldName,
    Tag.fieldName,
    ImageID.fieldName,
    Created.fieldName,
    Size.fieldName
  )
}
