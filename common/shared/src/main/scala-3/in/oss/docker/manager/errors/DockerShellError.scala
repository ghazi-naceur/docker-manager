package in.oss.docker.manager.errors

import in.oss.docker.manager.domain.Syntax.*

object DockerShellError {

  case class GetContainersError(logsHeader: String, expectedFields: List[String])
      extends Throwable(
        s"Get containers command is supposed to return these fields ${expectedFields.flat}, but returned these fields instead: '$logsHeader'"
      )

  case class GetImagesError(logsHeader: String, expectedFields: List[String])
      extends Throwable(
        s"Get images command is supposed to return these fields ${expectedFields.flat}, but returned these fields instead: '$logsHeader'"
      )

  case class ContainerStatusError(containerID: String, commandOutput: String)
      extends Throwable(s"Unexpected result when trying to change the status of the container '$containerID': '$commandOutput'")

  case class UnavailableContainer(containerID: String) extends Throwable(s"Container '$containerID' is not available")

  case class DockerFailure(error: String)
}
