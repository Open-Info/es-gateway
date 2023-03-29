package com.verify.esg

import cats.effect.Async
import cats.effect.kernel.Resource
import com.comcast.ip4s.{Host, Port}
import com.verify.esg.http.WalletRoute
import com.verify.esg.service.EsService
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.{Router, Server}

object Server {
  def build[F[_] : Async](esService: EsService[F], host: Host, port: Port): Resource[F, Server] = {
    val httpApp = Router("/" -> WalletRoute[F](esService).apply).orNotFound

    EmberServerBuilder.default[F]
      .withHost(host)
      .withPort(port)
      .withHttpApp(httpApp)
      .build
  }
}
