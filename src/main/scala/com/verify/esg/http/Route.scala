package com.verify.esg.http

import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl

trait Route[F[_]] extends Http4sDsl[F] {
  def apply: HttpRoutes[F]
}
