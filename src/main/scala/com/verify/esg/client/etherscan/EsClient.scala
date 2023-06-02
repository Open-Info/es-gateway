package com.verify.esg.client.etherscan

import cats.Parallel
import cats.effect.Sync
import cats.syntax.all._
import com.verify.esg.EsClientConfig
import com.verify.esg.client.etherscan.model._
import com.verify.esg.client.etherscan.model.response._
import com.verify.esg.model.EthAddressId
import io.chrisdavenport.cats.effect.time.JavaTime
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger
import sttp.client3._
import sttp.client3.circe._

import java.time.Instant

trait EsClient[F[_]] {
  def getTransactions(walletId: EthAddressId, numBlocks: Int): F[Set[EsTransaction]]
  def getTransactions(walletId: EthAddressId, start: Instant, end: Instant): F[Set[EsTransaction]]
  def getTransactions(walletId: EthAddressId, startBlock: Long, endBlock: Long): F[Set[EsTransaction]]
  def getBlock(unixEpoch: Long): F[Long]
  def getLatestBlock: F[Long]
  def getContractInfo(walletIds: Set[EthAddressId]): F[Set[EsContractInfo]]
}

object EsClient {
  def apply[F[_]: Sync: Parallel](esClientConfig: EsClientConfig, sttpBackend: SttpBackend[F, Any]): EsClient[F] =
    new EsClientImpl[F](esClientConfig, sttpBackend)

  private final class EsClientImpl[F[_]: Sync: Parallel](
    esClientConfig: EsClientConfig,
    sttpBackend: SttpBackend[F, Any]
  ) extends EsClient[F] {

    implicit val logger: Logger[F] = Slf4jLogger.getLogger[F]

    override def getTransactions(walletId: EthAddressId, numBlocks: Int): F[Set[EsTransaction]] =
      getLatestBlock.flatMap(b => getTransactions(walletId, b - numBlocks, b))

    override def getTransactions(walletId: EthAddressId, start: Instant, end: Instant): F[Set[EsTransaction]] =
      (getBlock(start.getEpochSecond), getBlock(end.getEpochSecond))
        .parFlatMapN(getTransactions(walletId, _, _))

    override def getTransactions(walletId: EthAddressId, startBlock: Long, endBlock: Long): F[Set[EsTransaction]] = {
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

      Logger[F].debug(s"Sending request GET etherscan.getblocknobytime for unixEpoch: $unixEpoch") *>
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

      def helper(walletIds: Set[EthAddressId]): F[Set[EsContractInfo]] = {
        val responseF =
          reqF(walletIds)
            .sendAndHandle(sttpBackend)
            .map(_.result.getOrElse(Set.empty))

        Logger[F].debug(s"Sending request GET etherscan.getcontractcreation for walletIds: ${walletIds.mkString(", ")}") *>
          responseF
      }

      Logger[F].debug(s"Retrieving contract info for ${walletIds.size} walletIds") *>
        walletIds
          .grouped(5) // we are only able to query 5 walletIds at a time with this endpoint
          .toVector
          .traverse { walletIds =>
            helper(walletIds)
              .handleErrorWith { e =>
                Logger[F]
                  .error(e)(s"Error while getting contract info from Etherscan - returning Set.empty")
                  .as(Set.empty[EsContractInfo]) // log and discard error with empty set (traverse will succeed)
              }
          }
          .map(_.flatten.toSet)
    }
  }
}
