package in.oss.docker.manager.cli

import cats.effect.IO
import in.oss.docker.manager.domain.*
import in.oss.docker.manager.errors.DockerShellError.{
  ContainerStatusError,
  GetContainersError,
  GetImagesError,
  UnavailableContainer
}
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger
import weaver.SimpleIOSuite

object DockerCLISpec extends SimpleIOSuite {

  given logger: Logger[IO] = Slf4jLogger.getLogger[IO]

  val unit: Unit = ()

  val containerID            = "587e8f1c0dcb"
  val getStopContainerOutput = "587e8f1c0dcb\n"

  val getContainersCommandOutput: String =
    """CONTAINER ID   IMAGE                      COMMAND                  CREATED       STATUS                    PORTS                                       NAMES              SIZE
      |587e8f1c0dcb   data-highway-app:v0.6-rc   "java -cp /app/jar/d…"   4 days ago    Exited (143) 4 days ago                                               bungee-gum         2.63kB (virtual 792MB)
      |008b5bcae48f   postgres                   "docker-entrypoint.s…"   8 weeks ago   Up 5 hours                0.0.0.0:5432->5432/tcp, :::5432->5432/tcp   scala-stack_db_1   63B (virtual 417MB)
      |""".stripMargin

  val container: Container = Container(
    ContainerID("587e8f1c0dcb"),
    ImageName("data-highway-app:v0.6-rc"),
    Command("\"java -cp /app/jar/d…\""),
    Created("4 days ago"),
    Status("Exited (143) 4 days ago"),
    Ports(""),
    Names("bungee-gum"),
    Size("2.63kB (virtual 792MB)")
  )

  val containers: List[Container] =
    List(
      container,
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

  val images: List[Image] = List(
    Image(Repository("data-highway-app"), Tag("v0.6-rc"), ImageID("67571ac37859"), Created("6 days ago"), Size("792MB")),
    Image(Repository("<none>"), Tag("<none>"), ImageID("37ab67d4eb59"), Created("6 days ago"), Size("501MB")),
    Image(Repository("demo"), Tag("latest"), ImageID("4c679f69d306"), Created("12 days ago"), Size("303MB"))
  )

  test("It should be able to list all containers") {
    val stub = new CommandExecutor[IO] {
      override def execute(command: Seq[String]): IO[String] = IO(getContainersCommandOutput)
    }

    val service = DockerCLI.impl[IO](stub)
    for {
      result <- service.getContainers
    } yield expect(result == containers)
  }

  test("It should be able to list all images") {
    val stub = new CommandExecutor[IO] {
      override def execute(command: Seq[String]): IO[String] = IO(getImagesCommandOutput)
    }

    val service = DockerCLI.impl[IO](stub)
    for {
      result <- service.getImages
    } yield expect(result == images)
  }
  
  test("It should be able to get all containers") {
    val result = DockerCLI.extractGetContainersResult[IO](getContainersCommandOutput)
    result.map(containers => expect(containers == containers))
  }

  test("It should return an error when encountering an issue while trying to get all containers") {
    val result = DockerCLI.extractGetContainersResult[IO]("unknown error").attempt
    result.map(images => expect(images == Left(GetContainersError("unknown error", Container.containerFields))))
  }

  test("It should be able to get all images") {
    val result = DockerCLI.extractGetImagesResult[IO](getImagesCommandOutput)
    result.map(images => expect(images == images))
  }

  test("It should return an error when encountering an issue while trying to get all images") {
    val result = DockerCLI.extractGetImagesResult[IO]("unknown error").attempt
    result.map(images => expect(images == Left(GetImagesError("unknown error", Image.imageFields))))
  }

  test("It should be able to check that the container has been stopped or started") {
    val containerId   = "587e8f1c0dcb"
    val commandOutput = "587e8f1c0dcb\n"
    val result        = DockerCLI.checkContainerStatusResult[IO](containerId, commandOutput)
    result.map(output => expect(output == unit))
  }

  test("It should return an error when encountering an issue while trying to stop or start a container") {
    val containerId   = "587e8f1c0dcb"
    val commandOutput = "unknown result"
    val result        = DockerCLI.checkContainerStatusResult[IO](containerId, commandOutput).attempt
    result.map(output => expect(output == Left(ContainerStatusError(containerId, commandOutput))))
  }

  test("It should be able to get container by id") {
    val containerId = "587e8f1c0dcb"
    val result      = DockerCLI.getContainer(containerId, containers)
    result.map(cont => expect(cont == container))
  }

  test("It should return an error when unable to find the container") {
    val containerId = "unknown container"
    val result      = DockerCLI.getContainer(containerId, containers).attempt
    result.map(output => expect(output == Left(UnavailableContainer(containerId))))
  }
}
