ThisBuild / version := "0.1.0-SNAPSHOT"

val scala3Version = "3.4.0-RC4"

ThisBuild / scalaVersion := scala3Version

lazy val tyrianVersion     = "0.6.1"
lazy val fs2DomVersion     = "0.1.0"
lazy val laikaVersion      = "0.19.0"
lazy val circeVersion      = "0.14.5"
lazy val catsEffectVersion = "3.5.0"
lazy val http4sVersion     = "0.23.19"
lazy val pureConfigVersion = "0.17.2"
lazy val log4catsVersion   = "2.5.0"
lazy val scalaTestVersion  = "3.2.15"
lazy val logbackVersion    = "1.4.7"
lazy val slf4jVersion      = "2.0.5"

// Common module
lazy val common = (crossProject(JSPlatform, JVMPlatform) in file("common"))
  .settings(
    name         := "common",
    scalaVersion := scala3Version,
    libraryDependencies ++= Seq(
      "io.circe" %%% "circe-core"    % circeVersion,
      "io.circe" %%% "circe-parser"  % circeVersion,
      "io.circe" %%% "circe-generic" % circeVersion
    )
  )

// Frontend module

lazy val frontend = (project in file("frontend"))
  .enablePlugins(ScalaJSPlugin)
  .settings(
    name         := "frontend",
    scalaVersion := scala3Version,
    libraryDependencies ++= Seq(
      "io.indigoengine" %%% "tyrian-io"  % tyrianVersion,
      "com.armanbilge"  %%% "fs2-dom"    % fs2DomVersion,
      "org.planet42"    %%% "laika-core" % laikaVersion
    ),
    scalaJSLinkerConfig ~= { _.withModuleKind(ModuleKind.CommonJSModule) },
    semanticdbEnabled := true,
    autoAPIMappings   := true
  )
  .dependsOn(common.js)

// Backend module

lazy val backend = (project in file("backend"))
  .settings(
    name         := "backend",
    scalaVersion := scala3Version,
    libraryDependencies ++= Seq(
      "org.typelevel"         %% "cats-effect"         % catsEffectVersion,
      "org.http4s"            %% "http4s-dsl"          % http4sVersion,
      "org.http4s"            %% "http4s-ember-server" % http4sVersion,
      "org.http4s"            %% "http4s-circe"        % http4sVersion,
      "com.github.pureconfig" %% "pureconfig-core"     % pureConfigVersion,
      "org.slf4j"              % "slf4j-simple"        % slf4jVersion,
      "org.typelevel"         %% "log4cats-slf4j"      % log4catsVersion,
      "org.typelevel"         %% "log4cats-noop"       % log4catsVersion  % Test,
      "ch.qos.logback"         % "logback-classic"     % logbackVersion   % Test,
      "org.scalatest"         %% "scalatest"           % scalaTestVersion % Test
    ),
    Compile / mainClass := Some("in.io.docker.manager.Main")
  )
  .dependsOn(common.jvm)
