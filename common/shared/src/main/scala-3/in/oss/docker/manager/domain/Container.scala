package in.oss.docker.manager.domain

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
