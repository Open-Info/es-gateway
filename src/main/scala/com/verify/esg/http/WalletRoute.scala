package com.verify.esg.http

import cats.effect.Async
import cats.implicits.toFlatMapOps
import com.verify.esg.service.EsService
import org.http4s.HttpRoutes
import org.http4s.circe.CirceEntityCodec.circeEntityEncoder

import scala.util.matching.Regex

final case class WalletRoute[F[_] : Async](esService: EsService[F]) extends Route[F] {
  object WalletIdVar {
    private val regex: Regex = "^0x[0-9a-fA-F]{40}$".r

    def unapply(str: String): Option[String] =
      Option.when(str.matches(regex.regex))(str)
  }

  override val route: HttpRoutes[F] = HttpRoutes.of[F] {
    case _@GET -> Root / WalletIdVar(walletId) / "friend" =>
      esService.getFriends(walletId).flatMap(items => Ok(items))

    case _@GET -> Root / WalletIdVar(walletId) / "transaction" =>
      esService.getTransactions(walletId).flatMap(items => Ok(items))
  }
}
