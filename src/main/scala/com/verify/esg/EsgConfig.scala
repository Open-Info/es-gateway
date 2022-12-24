package com.verify.esg

import cats.effect.Sync
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

final case class EsgConfig(
  esClientConfig: EsClientConfig
)

object EsgConfig {
  // Taken from sttp pureconfig module
  implicit val uriReader: ConfigReader[Uri] =
    ConfigReader.fromNonEmptyString { str =>
      Try(uri"$str") match {
        case Success(uri) => Right(uri)
        case Failure(ex) => Left(CannotConvert(str, "sttp.model.Uri", ex.getMessage))
      }
    }

  def load[F[_] : Sync]: F[EsgConfig] = ConfigSource.default.loadF[F, EsgConfig]()
}
