package com.verify.esg.client

import cats.effect.Sync
import cats.implicits._
import com.verify.esg.EsClientConfig
import com.verify.esg.model.etherscan.TransactionsResponse
import com.verify.esg.model.{ClientError, DeserializationError, SttpError, HttpError => DomHttpError}
import io.circe.generic.auto._
import org.typelevel.log4cats.slf4j.Slf4jLogger
import org.typelevel.log4cats.{Logger, SelfAwareStructuredLogger}
import sttp.client3._
import sttp.client3.circe._

trait EsClient[F[_]] {
  def getTransactions(walletId: String): F[Either[ClientError, TransactionsResponse]]
}

class EsClientImpl[F[_] : Sync](
  esClientConfig: EsClientConfig
)(implicit sttpBackend: SttpBackend[F, Any]) extends EsClient[F] {
  implicit def unsafeLogger: SelfAwareStructuredLogger[F] = Slf4jLogger.getLogger[F]

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

    val responseF = req.send(sttpBackend).redeem(
      e => SttpError(e).asLeft,
      r => r.body.leftMap {
        case DeserializationException(_, e) => DeserializationError(e)
        case HttpError(_, statusCode) => DomHttpError(statusCode)
      }
    )

    for {
      _ <- Logger[F].debug(s"Sending request GET etherscan.txlist for walletId $walletId")
      response <- responseF
      _ <- Logger[F].debug(s"GET etherscan.txlist request complete for walletId $walletId")
    } yield response
  }
}
