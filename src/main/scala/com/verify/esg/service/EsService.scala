package com.verify.esg.service

import cats.effect.{Clock, Sync}
import cats.syntax.all._
import com.verify.esg.EsServiceConfig
import com.verify.esg.client.etherscan.EsClient
import com.verify.esg.model.{EthAddressId, EthContract, EthTransaction}
import com.verify.esg.neo.TransactionNeo

trait EsService[F[_]] {
  def getFriends(walletId: EthAddressId): F[Set[EthAddressId]]
  def getTransactions(walletId: EthAddressId): F[Vector[EthTransaction]]
}

object EsService {
  def apply[F[_]: Sync: Clock](
    esClient: EsClient[F],
    transactionNeo: TransactionNeo[F],
    config: EsServiceConfig
  ): EsService[F] = new EsServiceImpl[F](esClient, transactionNeo, config)

  private final class EsServiceImpl[F[_]: Sync: Clock](
    esClient: EsClient[F],
    transactionNeo: TransactionNeo[F],
    config: EsServiceConfig
  ) extends EsService[F] {
    override def getFriends(walletId: EthAddressId): F[Set[EthAddressId]] =
      transactionsAndStore(walletId).map { transactions =>
        val wallets = transactions.flatMap(t => Vector(t.to.addressId, t.from.addressId)).toSet
        wallets - walletId
      }

    override def getTransactions(walletId: EthAddressId): F[Vector[EthTransaction]] = transactionsAndStore(walletId)

    private def transactionsAndStore(walletId: EthAddressId): F[Vector[EthTransaction]] =
      for {
        esTransactions  <- esClient.getLastNBlocksT(walletId, config.numBlocks)
        neighbours      <- transactionNeo.neighbours(walletId)
        neighbourContracts = neighbours.filter(_.isInstanceOf[EthContract])
        walletIds        = esTransactions.flatMap(t => t.to.toVector :+ t.from).toSet -- neighbours.map(_.addressId)
        esContractInfo  <- esClient.getContractInfo(walletIds)
        ethTransactions  = esTransactions.flatMap(EthTransaction.build(_, esContractInfo.map(_.contractAddress) ++ neighbourContracts.map(_.addressId)))
        _               <- transactionNeo.pushTransactions(ethTransactions)
      } yield ethTransactions
  }
}
