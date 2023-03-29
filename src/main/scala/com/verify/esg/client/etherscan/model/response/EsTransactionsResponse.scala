package com.verify.esg.client.etherscan.model.response

import com.verify.esg.client.etherscan.model.EsTransaction
import io.circe.Decoder
import io.circe.generic.semiauto.deriveDecoder

final case class EsTransactionsResponse(
  status: String,
  message: String,
  result: Vector[EsTransaction]
) extends EsResponse[Vector[EsTransaction]]

object EsTransactionsResponse {
  implicit val decoder: Decoder[EsTransactionsResponse] = deriveDecoder
}
