package com.verify.esg.model

import sttp.model.StatusCode

sealed abstract class AppError(val message: String, val cause: Option[Throwable]) extends Exception(message, cause.orNull)

sealed abstract class ClientError(
  override val message: String,
  override val cause: Option[Throwable] = None
) extends AppError(message, cause)

final case class DeserializationError(error: Throwable) extends ClientError(
  message = s"Failed to deserialize response with message: ${error.getMessage}",
  cause = Some(error)
)

final case class SttpError(error: Throwable) extends ClientError(
  message = "Failed to send request",
  cause = Some(error)
)

final case class HttpError(statusCode: StatusCode) extends ClientError(
  message = s"Received unexpected status code $statusCode"
)
