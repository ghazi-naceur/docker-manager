ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.4.0-RC4"

lazy val root = (project in file("."))
  .settings(
    name := "docker-manager"
  )
