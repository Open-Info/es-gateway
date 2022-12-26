package com.verify.esg.http

import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl

trait EsgRoute[F[_]] extends Http4sDsl[F] {
  val route: HttpRoutes[F]
}
