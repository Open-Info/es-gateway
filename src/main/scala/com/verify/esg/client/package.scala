package com.verify.esg

import cats.MonadThrow
import cats.syntax.all._
import com.verify.esg.model.{DeserializationError, SttpError, HttpError => DomHttpError}
import io.circe
import sttp.client3._

package object client {
  implicit final class RequestTOps[F[_]: MonadThrow, A](
    private val requestT: RequestT[Identity, Either[ResponseException[String, circe.Error], A], Any]
  ) {
    def sendAndHandle(sttpBackend: SttpBackend[F, Any]): F[A] =
      requestT
        .send(sttpBackend)
        .redeem(
          e => SttpError(e.getCause).asLeft,
          r => r.body.leftMap {
            case DeserializationException(_, e) => DeserializationError(e)
            case HttpError(_, statusCode) => DomHttpError(statusCode)
          }
        )
        .rethrow
  }
}
