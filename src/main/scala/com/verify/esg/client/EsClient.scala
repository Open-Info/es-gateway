package com.verify.esg.client

import cats.effect.Async
import cats.implicits._
import com.verify.esg.EsClientConfig
import com.verify.esg.model.etherscan.TransactionsResponse
import com.verify.esg.model.{ClientError, DeserializationError, SttpError, HttpError => DomHttpError}
import io.circe.generic.auto._
import sttp.client3._
import sttp.client3.circe._

trait EsClient[F[_]] {
  def getTransactions(walletId: String): F[Either[ClientError, TransactionsResponse]]
}

class EsClientImpl[F[_] : Async](
  esClientConfig: EsClientConfig
)(implicit sttpBackend: SttpBackend[F, Any]) extends EsClient[F] {
  override def getTransactions(walletId: String): F[Either[ClientError, TransactionsResponse]] = {
    val uri = esClientConfig.uri
      .withParams(
        ("module", "account"),
        ("action", "txlist"),
        ("address", walletId),
        ("apikey", esClientConfig.apiKey)
      )

    val req = basicRequest
      .get(uri)
      .response(asJson[TransactionsResponse])

    req.send(sttpBackend).redeem(
      e => SttpError(e).asLeft,
      r => r.body.leftMap {
        case DeserializationException(_, e) => DeserializationError(e)
        case HttpError(_, statusCode) => DomHttpError(statusCode)
      }
    )
  }
}
