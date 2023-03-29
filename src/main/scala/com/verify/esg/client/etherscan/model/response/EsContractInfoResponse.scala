package com.verify.esg.client.etherscan.model.response

import com.verify.esg.client.etherscan.model.EsContractInfo
import io.circe.Decoder
import io.circe.generic.semiauto.deriveDecoder

final case class EsContractInfoResponse(
  status: String,
  message: String,
  result: Option[Vector[EsContractInfo]]
) extends EsResponse[Option[Vector[EsContractInfo]]]

object EsContractInfoResponse {
  implicit val decoder: Decoder[EsContractInfoResponse] = deriveDecoder
}
