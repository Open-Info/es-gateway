package com.verify.esg.service

import cats.MonadError
import cats.implicits._
import com.verify.esg.client.EsClient
import com.verify.esg.model.etherscan.Transaction

trait EsService[F[_]] {
  def getFriends(walletId: String): F[Set[String]]

  def getTransactions(walletId: String): F[Vector[Transaction]]
}

final class EsServiceImpl[F[_]](esClient: EsClient[F])(implicit me: MonadError[F, Throwable]) extends EsService[F] {
  override def getFriends(walletId: String): F[Set[String]] = {
    esClient.getTransactions(walletId).flatMap {
      case Right(response) =>
        val wallets = response.result.foldLeft(Set.empty[String])((acc, t) => acc + t.to + t.from)
        (wallets - walletId).pure[F]
      case Left(e) =>
        e.ex.raiseError[F, Set[String]]
    }
  }

  override def getTransactions(walletId: String): F[Vector[Transaction]] =
    esClient.getTransactions(walletId).flatMap {
      case Right(response) => response.result.pure[F]
      case Left(e) => e.ex.raiseError[F, Vector[Transaction]]
    }
}
