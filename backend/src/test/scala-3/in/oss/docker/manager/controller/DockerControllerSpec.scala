package in.oss.docker.manager.controller

import cats.effect.*
import cats.implicits.*
import org.http4s
import org.http4s.*
import org.http4s.implicits.*
import org.http4s.circe.CirceEntityCodec.*
import io.circe.generic.auto.*
import cats.effect.IO
import in.oss.docker.manager.cli.DockerCLI
import in.oss.docker.manager.domain
import in.oss.docker.manager.domain.*
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger
import weaver.SimpleIOSuite

object DockerControllerSpec extends SimpleIOSuite {

  val unit: Unit = ()

  given logger: Logger[IO] = Slf4jLogger.getLogger[IO]

  val container: Container = Container(
    ContainerID("587e8f1c0dcb"),
    ImageName("data-highway-app:v0.6-rc"),
    Command("\"java -cp /app/jar/d…\""),
    Created("4 days ago"),
    domain.Status("Exited (143) 4 days ago"),
    Ports(""),
    Names("bungee-gum"),
    Size("2.63kB (virtual 792MB)")
  )
  val expectedContainers: List[Container] =
    List(
      container,
      Container(
        ContainerID("008b5bcae48f"),
        ImageName("postgres"),
        Command("\"docker-entrypoint.s…\""),
        Created("8 weeks ago"),
        domain.Status("Up 5 hours"),
        Ports("0.0.0.0:5432->5432/tcp, :::5432->5432/tcp"),
        Names("scala-stack_db_1"),
        Size("63B (virtual 417MB)")
      )
    )

  val expectedImages: List[Image] = List(
    Image(Repository("data-highway-app"), Tag("v0.6-rc"), ImageID("67571ac37859"), Created("6 days ago"), Size("792MB")),
    Image(Repository("<none>"), Tag("<none>"), ImageID("37ab67d4eb59"), Created("6 days ago"), Size("501MB")),
    Image(Repository("demo"), Tag("latest"), ImageID("4c679f69d306"), Created("12 days ago"), Size("303MB"))
  )

  test("It should be able to return all containers") {
    val dockerCLI: DockerCLI[IO] = new DockerCLI[IO] {
      override def getContainers: IO[List[Container]] = IO(expectedContainers)

      override def getImages: IO[List[Image]] = ???

      override def stopContainer(containerID: String): IO[Container] = ???
    }

    val dockerControllerRoutes: HttpApp[IO] = DockerController[IO](dockerCLI).routes

    for {
      responseOk <- dockerControllerRoutes.run(
        Request[IO](method = Method.GET, uri = uri"/docker/containers")
      )
      responseNotFound <- dockerControllerRoutes.run(
        Request[IO](method = Method.GET, uri = uri"/docker/containers/1")
      )
      retrieved <- responseOk.as[List[Container]]
    } yield {
      expect(
        responseOk.status == http4s.Status.Ok &&
          retrieved == expectedContainers &&
          responseNotFound.status == http4s.Status.NotFound
      )
    }
  }

  test("It should be able to return an error when unable to return the containers") {
    val dockerCLI: DockerCLI[IO] = new DockerCLI[IO] {
      override def getContainers: IO[List[Container]] = new Throwable("Error occurred").raiseError

      override def getImages: IO[List[Image]] = ???

      override def stopContainer(containerID: String): IO[Container] = ???
    }

    val dockerControllerRoutes: HttpApp[IO] = DockerController[IO](dockerCLI).routes

    for {
      response <- dockerControllerRoutes.run(
        Request[IO](method = Method.GET, uri = uri"/docker/containers")
      )
    } yield {
      expect(response.status == http4s.Status.InternalServerError)
    }
  }

  test("It should be able to return all images") {
    val dockerCLI: DockerCLI[IO] = new DockerCLI[IO] {
      override def getContainers: IO[List[Container]] = ???

      override def getImages: IO[List[Image]] = IO(expectedImages)

      override def stopContainer(containerID: String): IO[Container] = ???
    }

    val dockerControllerRoutes: HttpApp[IO] = DockerController[IO](dockerCLI).routes

    for {
      responseOk <- dockerControllerRoutes.run(
        Request[IO](method = Method.GET, uri = uri"/docker/images")
      )
      responseNotFound <- dockerControllerRoutes.run(
        Request[IO](method = Method.GET, uri = uri"/docker/images/1")
      )
      retrieved <- responseOk.as[List[Image]]
    } yield {
      expect(
        responseOk.status == http4s.Status.Ok &&
          retrieved == expectedImages &&
          responseNotFound.status == http4s.Status.NotFound
      )
    }
  }

  test("It should be able to return an error when unable to return the images") {
    val dockerCLI: DockerCLI[IO] = new DockerCLI[IO] {
      override def getContainers: IO[List[Container]] = ???

      override def getImages: IO[List[Image]] = new Throwable("Error occurred").raiseError

      override def stopContainer(containerID: String): IO[Container] = ???
    }

    val dockerControllerRoutes: HttpApp[IO] = DockerController[IO](dockerCLI).routes

    for {
      response <- dockerControllerRoutes.run(
        Request[IO](method = Method.GET, uri = uri"/docker/images")
      )
    } yield {
      expect(response.status == http4s.Status.InternalServerError)
    }
  }

  test("It should be able to stop a container") {
    val dockerCLI: DockerCLI[IO] = new DockerCLI[IO] {
      override def getContainers: IO[List[Container]] = ???

      override def getImages: IO[List[Image]] = ???

      override def stopContainer(containerID: String): IO[Container] = IO(container)
    }

    val dockerControllerRoutes: HttpApp[IO] = DockerController[IO](dockerCLI).routes

    for {
      responseOk <- dockerControllerRoutes.run(
        Request[IO](method = Method.PUT, uri = uri"/docker/container/containerID")
      )
      responseNotFound <- dockerControllerRoutes.run(
        Request[IO](method = Method.PUT, uri = uri"/docker/container/containerID/1")
      )
      retrieved <- responseOk.as[Container]
    } yield {
      expect(
        responseOk.status == http4s.Status.Ok &&
          retrieved == container &&
          responseNotFound.status == http4s.Status.NotFound
      )
    }
  }

  test("It should be able to return an error when unable to stop a container") {
    val dockerCLI: DockerCLI[IO] = new DockerCLI[IO] {
      override def getContainers: IO[List[Container]] = ???

      override def getImages: IO[List[Image]] = ???

      override def stopContainer(containerID: String): IO[Container] = new Throwable("Error occurred").raiseError
    }

    val dockerControllerRoutes: HttpApp[IO] = DockerController[IO](dockerCLI).routes

    for {
      response <- dockerControllerRoutes.run(
        Request[IO](method = Method.PUT, uri = uri"/docker/container/containerID")
      )
    } yield {
      expect(response.status == http4s.Status.InternalServerError)
    }
  }
}
