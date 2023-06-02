package com.verify.esg.service

import cats.effect.{Clock, Sync}
import cats.syntax.all._
import com.verify.esg.EsServiceConfig
import com.verify.esg.client.etherscan.EsClient
import com.verify.esg.model.{EthAddressId, EthTransaction}

trait EsService[F[_]] {
  def getFriends(walletId: EthAddressId): F[Set[EthAddressId]]
  def getTransactions(walletId: EthAddressId): F[Set[EthTransaction]]
}

object EsService {
  def apply[F[_]: Sync: Clock](
    esClient: EsClient[F],
    config: EsServiceConfig
  ): EsService[F] =
    new EsServiceImpl[F](esClient, config)

  private final class EsServiceImpl[F[_]: Sync: Clock](
    esClient: EsClient[F],
    config: EsServiceConfig
  ) extends EsService[F] {

    override def getFriends(walletId: EthAddressId): F[Set[EthAddressId]] =
      transactions(walletId).map { transactions =>
        val wallets = transactions.flatMap(t => Vector(t.to.addressId, t.from.addressId))
        wallets - walletId
      }

    override def getTransactions(walletId: EthAddressId): F[Set[EthTransaction]] =
      transactions(walletId)

    private def transactions(walletId: EthAddressId): F[Set[EthTransaction]] =
      esClient
        .getTransactions(walletId, config.numBlocks)
        .map(_.flatMap(EthTransaction.build(_, None)))
  }
}
