package com.verify.esg.http

import cats.effect.Sync
import cats.syntax.all._
import com.verify.esg.model.EthAddressId
import com.verify.esg.service.EsService
import org.http4s.circe.CirceEntityCodec.circeEntityEncoder
import org.http4s.{HttpRoutes, Method}
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger

final case class WalletRoute[F[_]: Sync](esService: EsService[F]) extends Route[F] {
  implicit val logger: Logger[F] = Slf4jLogger.getLogger[F]

  private def logReceive(method: Method, name: String): F[Unit] =
    Logger[F].info(s"${method.name} -> $name")

  private object WalletIdVar {
    def unapply(str: String): Option[EthAddressId] = EthAddressId(str).toOption
  }

  override def apply: HttpRoutes[F] = HttpRoutes.of[F] {
    case GET -> Root / "wallet" / WalletIdVar(walletId) / "friend" =>
      val metricName = MetricName("GET /wallet/{walletId}/friend")
      lazy val resultF =
        for {
          _ <- logReceive(GET, s"/wallet/{walletId}/friend $walletId")
          friends <- esService.getFriends(walletId)
          response <- Ok(friends)
        } yield response

      resultF.handleErrorWith(rootHandler.apply(metricName))

    case GET -> Root / "wallet" / WalletIdVar(walletId) / "transaction" =>
      val metricName = MetricName("GET /wallet/{walletId}/transaction")
      lazy val responseF =
        for {
          _ <- logReceive(GET, s"/wallet/{walletId}/transaction $walletId")
          transactions <- esService.getTransactions(walletId)
          response <- Ok(transactions)
        } yield response

      responseF.handleErrorWith(rootHandler.apply(metricName))
  }
}

object WalletRoute {
  def apply[F[_]: Sync](esService: EsService[F]): WalletRoute[F] = new WalletRoute[F](esService)
}
