package com.verify.esg.neo

import cats.effect.kernel.Sync
import cats.syntax.all._
import com.verify.esg.model.EthTransaction
import com.verify.esg.neo.model.{Address, TransactedWith}
import neotypes.Driver
import neotypes.implicits.all.neotypesSyntaxCypherStringInterpolator

trait TransactionNeo[F[_]] {
  def pushTransactions(transactions: Vector[EthTransaction]): F[Unit]
}

object TransactionNeo {
  def apply[F[_]: Sync](driver: Driver[F]): TransactionNeo[F] = new TransactionNeoImpl(driver)

  private final class TransactionNeoImpl[F[_]: Sync](driver: Driver[F]) extends TransactionNeo[F] {
    override def pushTransactions(transactions: Vector[EthTransaction]): F[Unit] =
      transactions.traverse { transaction =>
        val toAddress = Address.fromEthAddress(transaction.to)
        val fromAddress = Address.fromEthAddress(transaction.from)
        val transactedWith = TransactedWith.fromEthTransaction(transaction)

        c"""
           MERGE (from:Address { $fromAddress })
           MERGE (to:Address { $toAddress })
           MERGE (from)-[t:TRANSACTED_WITH { $transactedWith }]->(to)
         """
          .query[Unit]
          .execute(driver)
      }.map(_ => ())
  }
}
