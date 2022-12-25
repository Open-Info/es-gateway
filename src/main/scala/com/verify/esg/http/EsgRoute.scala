package com.verify.esg.http

import cats.effect.Async
import cats.implicits.toFlatMapOps
import com.verify.esg.service.EsService
import org.http4s.HttpRoutes
import org.http4s.circe.CirceEntityCodec.circeEntityEncoder
import org.http4s.dsl.Http4sDsl

trait EsgRoute[F[_]] extends Http4sDsl[F] {
  val route: HttpRoutes[F]
}

object EsgRoute {
  def wallet[F[_]: Async](esService: EsService[F]): EsgRoute[F] = new EsgRoute[F] {
    override val route: HttpRoutes[F] = HttpRoutes.of[F] {
      case _@GET -> Root / walletId / "friends" =>
        esService.getFriends(walletId).flatMap(items => Ok(items))
    }
  }
}
