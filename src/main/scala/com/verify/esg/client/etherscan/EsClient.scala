package com.verify.esg.client.etherscan

import cats.effect.Sync
import cats.syntax.all._
import com.verify.esg.EsClientConfig
import com.verify.esg.client.etherscan.model.response.{EsBlockTimestampResponse, EsContractInfoResponse, EsTransactionsResponse}
import com.verify.esg.client.etherscan.model.{EsContractInfo, EsTransaction}
import com.verify.esg.model.EthAddressId
import io.chrisdavenport.cats.effect.time.JavaTime
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger
import sttp.client3._
import sttp.client3.circe._

import scala.util.Try

trait EsClient[F[_]] {
  def getLastNBlockTransactions(walletId: EthAddressId, n: Int): F[Vector[EsTransaction]]
  def getTransactions(walletId: EthAddressId, startBlock: Long, endBlock: Long): F[Vector[EsTransaction]]
  def getBlock(unixEpoch: Long): F[Long]
  def getLatestBlock: F[Long]
  def getContractInfo(walletIds: Set[EthAddressId]): F[Set[EsContractInfo]]
}

object EsClient {
  def apply[F[_]: Sync](esClientConfig: EsClientConfig, sttpBackend: SttpBackend[F, Any]): EsClient[F] =
    new EsClientImpl[F](esClientConfig, sttpBackend)

  private final class EsClientImpl[F[_]: Sync](
    esClientConfig: EsClientConfig,
    sttpBackend: SttpBackend[F, Any]
  ) extends EsClient[F] {
    implicit val logger: Logger[F] = Slf4jLogger.getLogger[F]

    override def getTransactions(
      walletId: EthAddressId,
      startBlock: Long,
      endBlock: Long
    ): F[Vector[EsTransaction]] = {
      val uri =
        esClientConfig.uri
          .withParams(
            ("module", "account"),
            ("action", "txlist"),
            ("address", walletId.value),
            ("startblock", startBlock.toString),
            ("endblock", endBlock.toString),
            ("sort", "desc"),
            ("apikey", esClientConfig.apiKey)
          )

      val req =
        basicRequest
          .get(uri)
          .response(asJson[EsTransactionsResponse])

      Logger[F].debug(s"Sending request GET etherscan.txlist for walletId: $walletId") *>
        req
          .sendAndHandle(sttpBackend)
          .map(_.result)
    }

    override def getLatestBlock: F[Long] =
      JavaTime[F]
        .getInstant
        .map(_.getEpochSecond)
        .flatMap(getBlock)

    override def getContractInfo(walletIds: Set[EthAddressId]): F[Set[EsContractInfo]] = {
      def uri(walletIds: Set[EthAddressId]) =
        esClientConfig.uri
          .withParams(
            ("module", "contract"),
            ("action", "getcontractcreation"),
            ("contractaddresses", walletIds.map(_.value).mkString(",")),
            ("apikey", esClientConfig.apiKey)
          )

      def reqF(walletIds: Set[EthAddressId]) =
        basicRequest
          .get(uri(walletIds))
          .response(asJson[EsContractInfoResponse])

      walletIds
        .grouped(5)
        .toVector
        .traverse { walletIds =>
          val responseF = reqF(walletIds).sendAndHandle(sttpBackend)
          Logger[F].debug(s"Sending request GET etherscan.getcontractcreation for walletIds: ${walletIds.mkString(", ")}") *> responseF
        }
        .map(_.flatMap(_.result).flatten.toSet)
    }

    override def getBlock(unixEpoch: Long): F[Long] = {
      val uri =
        esClientConfig.uri
          .withParams(
            ("module", "block"),
            ("action", "getblocknobytime"),
            ("timestamp", unixEpoch.toString),
            ("closest", "before"),
            ("apikey", esClientConfig.apiKey)
          )

      val req =
        basicRequest
          .get(uri)
          .response(asJson[EsBlockTimestampResponse])

      req
        .sendAndHandle(sttpBackend)
        .flatMap(r => Try(r.result.toLong).toEither.pure[F].rethrow)
    }

    override def getLastNBlockTransactions(walletId: EthAddressId, n: Int): F[Vector[EsTransaction]] =
      getLatestBlock.flatMap(b => getTransactions(walletId, b - n, b))
  }
}
