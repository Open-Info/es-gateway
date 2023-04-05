package com.verify.esg

import cats.Monad
import monix.newtypes.NewtypeWrapped
import org.http4s.circe.CirceEntityCodec.circeEntityEncoder
import org.typelevel.log4cats.Logger

import scala.util.control.NonFatal

package object http {
  type MetricName = MetricName.Type
  object MetricName extends NewtypeWrapped[String]

  private def defaultErrorResponseBody(t: Throwable): Map[String, String] = Map("error" -> t.getMessage)

  // I'd like to change this functionality to something more akin to this
  // https://typelevel.org/blog/2018/08/25/http4s-error-handling-mtl.html
  def rootHandler[F[_]: Logger: Monad]: ExceptionHandler[F] = new ExceptionHandler[F] {
    override val loggingF: LogLevelFunction[F] = {
      case NonFatal(_) => Logger[F].error
    }

    override val responseF: ResponseFunction[F] = {
      case NonFatal(e) => InternalServerError(defaultErrorResponseBody(e))
    }
  }
}
