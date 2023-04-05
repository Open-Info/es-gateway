package com.verify.esg.client.etherscan.model.response

import io.circe.Decoder
import io.circe.generic.semiauto.deriveDecoder

final case class EsBlockTimestampResponse(
  status: String,
  message: String,
  result: Long
) extends EsResponse[Long]

object EsBlockTimestampResponse {
  implicit val decoder: Decoder[EsBlockTimestampResponse] = deriveDecoder
}
