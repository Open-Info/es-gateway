package com.verify.esg.model

import cats.syntax.option._
import io.circe.{Error => CirceError}
import sttp.model.StatusCode

sealed trait AppError extends Exception {
  val message: String
  val cause: Option[Throwable]
}

sealed trait ClientError extends AppError

final case class DeserializationError(circeError: CirceError) extends ClientError {
  override val message: String = s"Deserialization failed"
  override val cause: Option[Throwable] = circeError.some
}

final case class SttpError(error: Throwable) extends ClientError {
  override val message: String = "Error encountered from sttp"
  override val cause: Option[Throwable] = error.some
}

final case class HttpError(statusCode: StatusCode) extends ClientError {
  override val message: String = s"Request failed with code ${statusCode.code}"
  override val cause: Option[Throwable] = None
}
