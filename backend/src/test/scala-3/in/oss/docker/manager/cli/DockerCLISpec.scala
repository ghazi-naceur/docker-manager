package in.oss.docker.manager.cli

import cats.effect.IO
import in.oss.docker.manager.domain.*
import in.oss.docker.manager.errors.DockerShellError.{GetContainersError, GetImagesError, StopContainerError}
import weaver.SimpleIOSuite

object DockerCLISpec extends SimpleIOSuite {

  val unit: Unit = ()

  val getContainersCommandOutput: String =
    """CONTAINER ID   IMAGE                      COMMAND                  CREATED       STATUS                    PORTS                                       NAMES              SIZE
      |587e8f1c0dcb   data-highway-app:v0.6-rc   "java -cp /app/jar/d…"   4 days ago    Exited (143) 4 days ago                                               bungee-gum         2.63kB (virtual 792MB)
      |008b5bcae48f   postgres                   "docker-entrypoint.s…"   8 weeks ago   Up 5 hours                0.0.0.0:5432->5432/tcp, :::5432->5432/tcp   scala-stack_db_1   63B (virtual 417MB)
      |""".stripMargin

  val expectedContainers: List[Container] =
    List(
      Container(
        ContainerID("587e8f1c0dcb"),
        ImageName("data-highway-app:v0.6-rc"),
        Command("\"java -cp /app/jar/d…\""),
        Created("4 days ago"),
        Status("Exited (143) 4 days ago"),
        Ports(""),
        Names("bungee-gum"),
        Size("2.63kB (virtual 792MB)")
      ),
      Container(
        ContainerID("008b5bcae48f"),
        ImageName("postgres"),
        Command("\"docker-entrypoint.s…\""),
        Created("8 weeks ago"),
        Status("Up 5 hours"),
        Ports("0.0.0.0:5432->5432/tcp, :::5432->5432/tcp"),
        Names("scala-stack_db_1"),
        Size("63B (virtual 417MB)")
      )
    )

  val getImagesCommandOutput: String =
    """REPOSITORY                 TAG                  IMAGE ID       CREATED         SIZE
      |data-highway-app           v0.6-rc              67571ac37859   6 days ago      792MB
      |<none>                     <none>               37ab67d4eb59   6 days ago      501MB
      |demo                       latest               4c679f69d306   12 days ago     303MB
      |""".stripMargin

  val expectedImages: List[Image] = List(
    Image(Repository("data-highway-app"), Tag("v0.6-rc"), ImageID("67571ac37859"), Created("6 days ago"), Size("792MB")),
    Image(Repository("<none>"), Tag("<none>"), ImageID("37ab67d4eb59"), Created("6 days ago"), Size("501MB")),
    Image(Repository("demo"), Tag("latest"), ImageID("4c679f69d306"), Created("12 days ago"), Size("303MB"))
  )

  test("It should be able to get all containers") {
    val result = DockerCLI.extractGetContainersResult[IO](getContainersCommandOutput)
    result.map(containers => expect(containers == expectedContainers))
  }

  test("It should return an error when encountering an issue while trying to get all containers") {
    val result = DockerCLI.extractGetContainersResult[IO]("unknown error").attempt
    result.map(images => expect(images == Left(GetContainersError("unknown error", Container.containerFields))))
  }

  test("It should be able to get all images") {
    val result = DockerCLI.extractGetImagesResult[IO](getImagesCommandOutput)
    result.map(images => expect(images == expectedImages))
  }

  test("It should return an error when encountering an issue while trying to get all images") {
    val result = DockerCLI.extractGetImagesResult[IO]("unknown error").attempt
    result.map(images => expect(images == Left(GetImagesError("unknown error", Image.imageFields))))
  }

  test("It should be able to extract the result of stopping a container") {
    val containerId   = "587e8f1c0dcb"
    val commandOutput = "587e8f1c0dcb\n"
    val result        = DockerCLI.extractStopContainerResult[IO](containerId, commandOutput)
    result.map(output => expect(output == unit))
  }

  test("It should return an error when encountering an issue while trying to stop a container") {
    val containerId   = "587e8f1c0dcb"
    val commandOutput = "unknown result"
    val result        = DockerCLI.extractStopContainerResult[IO](containerId, commandOutput).attempt
    result.map(output => expect(output == Left(StopContainerError(containerId, commandOutput))))
  }
}
