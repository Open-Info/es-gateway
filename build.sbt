ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / organization := "com.verify"
ThisBuild / scalaVersion := "2.13.10"

enablePlugins(sbtdocker.DockerPlugin, JavaAppPackaging)

lazy val sttpVersion = "3.8.5"
lazy val catsVersion = "2.9.0"
lazy val catsEffectVersion = "3.4.8"
lazy val circeVersion = "0.14.1"
lazy val pureconfigVersion = "0.17.2"
lazy val http4sVersion = "0.23.16"
lazy val logbackVersion = "1.4.6"
lazy val log4catsVersion = "2.5.0"
lazy val scalaTestVersion = "3.2.15"
lazy val newtypesVersion = "0.2.3"
lazy val scalaMockVersion = "5.2.0"

lazy val root = (project in file("."))
  .settings(
    name := "es-gateway",

    libraryDependencies ++= Seq(
      "com.softwaremill.sttp.client3" %% "core",
      "com.softwaremill.sttp.client3" %% "circe",
      "com.softwaremill.sttp.client3" %% "async-http-client-backend-cats"
    ).map(_ % sttpVersion),

    libraryDependencies += "org.typelevel" %% "cats-core" % catsVersion,
    libraryDependencies += "org.typelevel" %% "cats-effect" % catsEffectVersion,

    libraryDependencies ++= Seq(
      "io.monix" %% "newtypes-core",
      "io.monix" %% "newtypes-circe-v0-14"
    ).map(_ % newtypesVersion),

      libraryDependencies ++= Seq(
      "io.circe" %% "circe-core",
      "io.circe" %% "circe-generic",
      "io.circe" %% "circe-parser"
    ).map(_ % circeVersion),

    libraryDependencies ++= Seq(
      "com.github.pureconfig" %% "pureconfig",
      "com.github.pureconfig" %% "pureconfig-cats-effect"
    ).map(_ % pureconfigVersion),

    libraryDependencies ++= Seq(
      "org.http4s" %% "http4s-dsl",
      "org.http4s" %% "http4s-circe",
      "org.http4s" %% "http4s-ember-server"
    ).map(_ % http4sVersion),

    libraryDependencies += "ch.qos.logback" % "logback-classic" % logbackVersion % Runtime,

    libraryDependencies ++= Seq(
      "org.typelevel" %% "log4cats-core",
      "org.typelevel" %% "log4cats-slf4j",
    ).map(_ % log4catsVersion),

    libraryDependencies += "org.scalactic" %% "scalactic" % scalaTestVersion,
    libraryDependencies += "org.scalatest" %% "scalatest" % scalaTestVersion % Test,
    libraryDependencies += "org.scalamock" %% "scalamock" % scalaMockVersion % Test,

    docker / dockerfile := {
      val appDir: File = stage.value
      val targetDir = "/app"

      new Dockerfile {
        from("openjdk:11-jre")
        entryPoint(s"$targetDir/bin/${executableScriptName.value}")
        copy(appDir, targetDir, chown = "daemon:daemon")
      }
    }
  )
