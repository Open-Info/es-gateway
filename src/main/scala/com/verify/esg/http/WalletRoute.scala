package com.verify.esg.http

import cats.effect.Sync
import cats.implicits._
import com.verify.esg.service.EsService
import org.http4s.circe.CirceEntityCodec.circeEntityEncoder
import org.http4s.{HttpRoutes, Method}
import org.typelevel.log4cats.slf4j.Slf4jLogger
import org.typelevel.log4cats.{Logger, SelfAwareStructuredLogger}

import scala.util.matching.Regex

final case class WalletRoute[F[_] : Sync](esService: EsService[F]) extends Route[F] {
  implicit def unsafeLogger: SelfAwareStructuredLogger[F] = Slf4jLogger.getLogger[F]

  private def logReceive(method: Method, name: String): F[Unit] =
    Logger[F].info(s"${method.name} -> $name")

  object WalletIdVar {
    private val regex: Regex = "^0x[0-9a-fA-F]{40}$".r

    def unapply(str: String): Option[String] =
      Option.when(str.matches(regex.regex))(str)
  }

  override val route: HttpRoutes[F] = HttpRoutes.of[F] {
    case GET -> Root / WalletIdVar(walletId) / "friend" =>
      for {
        _ <- logReceive(GET, s"/wallet/{walletId}/friend $walletId")
        friends <- esService.getFriends(walletId)
        response <- Ok(friends)
      } yield response

    case GET -> Root / WalletIdVar(walletId) / "transaction" =>
      for {
        _ <- logReceive(GET, s"/wallet/{walletId}/transaction $walletId")
        transactions <- esService.getTransactions(walletId)
        response <- Ok(transactions)
      } yield response
  }
}
