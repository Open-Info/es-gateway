package com.verify.esg.model.etherscan

import io.circe.Decoder
import io.circe.generic.semiauto.deriveDecoder

final case class TransactionsResponse(
  status: String,
  message: String,
  result: Vector[Transaction]
)

object TransactionsResponse {
  implicit val decoder: Decoder[TransactionsResponse] = deriveDecoder
}
