package in.oss.docker.manager.domain

case class Image(
    repository: Repository,
    tag: Tag,
    imageId: ImageID,
    created: Created,
    size: Size
)
