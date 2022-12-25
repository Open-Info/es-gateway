package com.verify.esg.service

import cats.effect.Async
import cats.implicits._
import com.verify.esg.client.EsClient

trait EsService[F[_]] {
  def getFriends(walletId: String): F[Set[String]]
}

final class EsServiceImpl[F[_] : Async](esClient: EsClient[F]) extends EsService[F] {
  override def getFriends(walletId: String): F[Set[String]] = {
    esClient.getTransactions(walletId).flatMap {
      case Right(response) =>
        val wallets = response.result.foldLeft(Set.empty[String])((acc, t) => acc + t.to + t.from)
        (wallets - walletId).pure[F]
      case Left(e) =>
        e.ex.raiseError[F, Set[String]]
    }
  }
}
