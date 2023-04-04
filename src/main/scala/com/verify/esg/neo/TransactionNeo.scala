package com.verify.esg.neo

import cats.effect.kernel.Sync
import cats.syntax.all._
import com.verify.esg.model.{EthAddress, EthAddressId, EthTransaction}
import com.verify.esg.neo.model.{Address, TransactedWith}
import neotypes.Driver
import neotypes.implicits.all.neotypesSyntaxCypherStringInterpolator
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger

trait TransactionNeo[F[_]] {
  def pushTransactions(transactions: Vector[EthTransaction]): F[Unit]
  def neighbours(address: EthAddressId): F[Vector[EthAddress]]
}

object TransactionNeo {
  def apply[F[_]: Sync](driver: Driver[F]): TransactionNeo[F] = new TransactionNeoImpl(driver)

  private final class TransactionNeoImpl[F[_]: Sync](driver: Driver[F]) extends TransactionNeo[F] {
    implicit val logger: Logger[F] = Slf4jLogger.getLogger[F]

    override def pushTransactions(transactions: Vector[EthTransaction]): F[Unit] =
      Logger[F].debug(s"Pushing ${transactions.size} transactions to Neo4j") *>
        transactions.traverse { transaction =>
          val fromAddress = Address.fromEthAddress(transaction.from)
          val toAddress = Address.fromEthAddress(transaction.to)
          val transactedWith = TransactedWith.fromEthTransaction(transaction)

          c"""
              MERGE (from:Address { $fromAddress })
              MERGE (to:Address { $toAddress })
              MERGE (from)-[t:TRANSACTED_WITH { $transactedWith }]->(to)
              """
            .query[Unit]
            .execute(driver)
        }.map(_ => ())

    override def neighbours(addressId: EthAddressId): F[Vector[EthAddress]] =
      Logger[F].debug(s"Getting neighbours for walletId: ${addressId.value}") *> {
        c"""
            MATCH (:Address { ethAddressId: $addressId })--(b:Address) RETURN b
            """
          .readOnlyQuery[Address]
          .vector(driver)
          .map(_.map { address =>
            address.`type` match {
              case EthAddress.Type.Wallet => EthAddress.Wallet(address.ethAddressId)
              case EthAddress.Type.Contract => EthAddress.Contract(address.ethAddressId)
              case EthAddress.Type.Unknown => EthAddress.Unknown(address.ethAddressId)
            }
          })
      }
  }
}
