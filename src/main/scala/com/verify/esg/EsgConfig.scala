package com.verify.esg

import cats.effect.Sync
import com.comcast.ip4s.{Host, Port}
import pureconfig._
import pureconfig.error.CannotConvert
import pureconfig.generic.auto._
import pureconfig.module.catseffect.syntax._
import sttp.client3.UriContext
import sttp.model.Uri

import scala.util.{Failure, Success, Try}

final case class EsClientConfig(
  uri: Uri,
  apiKey: String
)

final case class ServerConfig(
  port: Port,
  host: Host
)

final case class EsgConfig(
  esClientConfig: EsClientConfig,
  serverConfig: ServerConfig
)

object EsgConfig {
  implicit val portReader: ConfigReader[Port] =
    ConfigReader.fromNonEmptyString { str =>
      Port.fromString(str) match {
        case Some(port) => Right(port)
        case None => Left(CannotConvert(str, "com.comcast.ip4s.Port", "Incorrect format"))
      }
    }

  implicit val hostReader: ConfigReader[Host] =
    ConfigReader.fromNonEmptyString { str =>
      Host.fromString(str) match {
        case Some(host) => Right(host)
        case None => Left(CannotConvert(str, "com.comcast.ip4s.Host", "Incorrect format"))
      }
    }

  implicit val uriReader: ConfigReader[Uri] =
    ConfigReader.fromNonEmptyString { str =>
      Try(uri"$str") match {
        case Success(uri) => Right(uri)
        case Failure(ex) => Left(CannotConvert(str, "sttp.model.Uri", ex.getMessage))
      }
    }

  def load[F[_] : Sync]: F[EsgConfig] = ConfigSource.default.loadF[F, EsgConfig]()
}
