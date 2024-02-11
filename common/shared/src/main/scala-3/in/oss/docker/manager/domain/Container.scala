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
