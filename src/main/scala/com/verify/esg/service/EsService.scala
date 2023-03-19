package com.verify.esg.service

import cats.Monad
import cats.syntax.all._
import com.verify.esg.client.EsClient
import com.verify.esg.model.etherscan.Transaction

trait EsService[F[_]] {
  def getFriends(walletId: String): F[Set[String]]
  def getTransactions(walletId: String): F[Vector[Transaction]]
}

object EsService {
  def apply[F[_]: Monad](esClient: EsClient[F]): EsService[F] = new EsServiceImpl[F](esClient)

  private final class EsServiceImpl[F[_] : Monad](esClient: EsClient[F]) extends EsService[F] {
    override def getFriends(walletId: String): F[Set[String]] =
      esClient.getTransactions(walletId).map { response =>
        val wallets = response.result.foldLeft(Set.empty[String])((acc, t) => acc + t.to + t.from)
        wallets - walletId
      }

    override def getTransactions(walletId: String): F[Vector[Transaction]] =
      esClient.getTransactions(walletId).map(_.result)
  }
}
