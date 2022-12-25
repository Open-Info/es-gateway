package com.verify.esg

import cats.effect.Async
import cats.effect.kernel.Resource
import com.comcast.ip4s.IpLiteralSyntax
import com.verify.esg.service.EsService
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.{Router, Server}
import com.verify.esg.http.EsgRoute.wallet

object Server {
  def build[F[_] : Async](esService: EsService[F]): Resource[F, Server] = {
    val httpApp = Router(
      "/wallet" -> wallet(esService).route
    ).orNotFound

    EmberServerBuilder.default[F]
      .withHost(ipv4"0.0.0.0")
      .withPort(port"80")
      .withHttpApp(httpApp)
      .build
  }
}
