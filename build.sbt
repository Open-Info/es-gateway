ThisBuild / version := "0.2.0-SNAPSHOT"
ThisBuild / organization := "com.verify"
ThisBuild / scalaVersion := "2.13.10"

enablePlugins(JavaAppPackaging)

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
lazy val neotypesVersion = "0.23.2"
lazy val neo4jVersion = "5.6.0"
lazy val catsEffectTimeVersion = "0.2.0"
lazy val enumeratumVersion = "1.7.2"

lazy val root = (project in file("."))
  .settings(
    name := "es-gateway",
    Compile / mainClass := Some("com.verify.esg.Main"),
    Compile / discoveredMainClasses := Seq.empty,

    libraryDependencies += "com.beachape" %% "enumeratum" % enumeratumVersion,

    libraryDependencies ++= Seq(
      "com.softwaremill.sttp.client3" %% "core",
      "com.softwaremill.sttp.client3" %% "circe",
      "com.softwaremill.sttp.client3" %% "async-http-client-backend-cats"
    ).map(_ % sttpVersion),

    libraryDependencies += "org.typelevel" %% "cats-core" % catsVersion,
    libraryDependencies += "org.typelevel" %% "cats-effect" % catsEffectVersion,

    libraryDependencies += "io.chrisdavenport" %% "cats-effect-time" % catsEffectTimeVersion,

    libraryDependencies ++= Seq(
      "io.monix" %% "newtypes-core",
      "io.monix" %% "newtypes-circe-v0-14"
    ).map(_ % newtypesVersion),

    libraryDependencies += "org.neo4j.driver" % "neo4j-java-driver" % neo4jVersion,

    libraryDependencies ++= Seq(
      "io.github.neotypes" %% "neotypes-core",
      "io.github.neotypes" %% "neotypes-generic",
      "io.github.neotypes" %% "neotypes-cats-effect",
      "io.github.neotypes" %% "neotypes-enumeratum"
    ).map(_ % neotypesVersion),

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
  )
