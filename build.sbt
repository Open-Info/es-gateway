ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / organization := "com.verify"
ThisBuild / scalaVersion := "2.13.10"

lazy val sttpVersion = "3.8.5"
lazy val catsVersion = "2.9.0"
lazy val catsEffectVersion = "3.4.2"
lazy val circeVersion = "0.14.1"
lazy val pureconfigVersion = "0.17.2"
lazy val http4sVersion = "0.23.16"

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
  )
