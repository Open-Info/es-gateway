package com.verify.esg.http

import cats.implicits.toFlatMapOps
import cats.{Applicative, Monad}
import org.http4s.Response
import org.http4s.circe.CirceEntityCodec.circeEntityEncoder
import org.http4s.dsl.Http4sDsl
import org.typelevel.log4cats.Logger

trait ExceptionHandler[F[_]] extends Http4sDsl[F] {
  type LogLevelFunction[G[_]] = PartialFunction[Throwable, Throwable => (=> String) => G[Unit]]
  type ResponseFunction[G[_]] = PartialFunction[Throwable, G[Response[G]]]

  def loggingF: LogLevelFunction[F]
  def responseF: ResponseFunction[F]

  def apply(metricName: MetricName)(implicit m: Monad[F], logger: Logger[F]): PartialFunction[Throwable, F[Response[F]]] =
    e => handleError(e).flatTap(r => logError(e, metricName, r))

  def orElse(that: ExceptionHandler[F]): ExceptionHandler[F] = ExceptionHandler.orElse(this, that)

  private def handleError(e: Throwable)(implicit a: Applicative[F]): F[Response[F]] =
    responseF.lift(e).getOrElse(InternalServerError(Map("error" -> e.getMessage)))

  private def logError(e: Throwable, metricName: MetricName, response: Response[F])(implicit l: Logger[F]): F[Unit] = {
    val message = s"Operation: ${metricName.value} failed - returning ${response.status.code}"
    loggingF.lift(e).map(_(e)(message)).getOrElse(Logger[F].error(e)(message))
  }
}

object ExceptionHandler {
  def orElse[F[_]](`this`: ExceptionHandler[F], that: ExceptionHandler[F]): ExceptionHandler[F] = new ExceptionHandler[F] {
    override def loggingF: LogLevelFunction[F] = `this`.loggingF orElse that.loggingF
    override def responseF: ResponseFunction[F] = `this`.responseF orElse that.responseF
  }
}
