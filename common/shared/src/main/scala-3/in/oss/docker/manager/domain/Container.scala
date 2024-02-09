package in.oss.docker.manager.domain

case class Container(
    containerId: CONTAINER_ID,
    image: IMAGE,
    command: COMMAND,
    created: CREATED,
    status: STATUS,
    ports: PORTS,
    names: NAMES
)
