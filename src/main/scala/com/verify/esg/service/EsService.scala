package com.verify.esg.service

import cats.effect.{Clock, Sync}
import cats.syntax.all._
import com.verify.esg.EsServiceConfig
import com.verify.esg.client.etherscan.EsClient
import com.verify.esg.model.{EthAddressId, EthTransaction}
import com.verify.esg.neo.TransactionNeo

trait EsService[F[_]] {
  def getFriends(walletId: EthAddressId): F[Set[EthAddressId]]
  def getTransactions(walletId: EthAddressId): F[Set[EthTransaction]]
}

object EsService {
  def apply[F[_]: Sync: Clock](
    esClient: EsClient[F],
    transactionNeo: TransactionNeo[F],
    config: EsServiceConfig
  ): EsService[F] =
    new EsServiceImpl[F](esClient, transactionNeo, config)

  private final class EsServiceImpl[F[_]: Sync: Clock](
    esClient: EsClient[F],
    transactionNeo: TransactionNeo[F],
    config: EsServiceConfig
  ) extends EsService[F] {

    override def getFriends(walletId: EthAddressId): F[Set[EthAddressId]] =
      transactionsAndStore(walletId).map { transactions =>
        val wallets = transactions.flatMap(t => Vector(t.to.addressId, t.from.addressId))
        wallets - walletId
      }

    override def getTransactions(walletId: EthAddressId): F[Set[EthTransaction]] =
      transactionsAndStore(walletId)

    private def transactionsAndStore(walletId: EthAddressId): F[Set[EthTransaction]] =
      for {
        esTransactions  <- esClient.getTransactions(walletId, config.numBlocks)
        ethTransactions  = esTransactions.flatMap(EthTransaction.build(_, None))
        _               <- transactionNeo.pushTransactions(ethTransactions)
      } yield ethTransactions
  }
}
