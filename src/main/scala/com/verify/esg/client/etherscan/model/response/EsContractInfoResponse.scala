package com.verify.esg.client.etherscan.model.response

import com.verify.esg.client.etherscan.model.EsContractInfo
import io.circe.Decoder
import io.circe.generic.semiauto.deriveDecoder

final case class EsContractInfoResponse(
  status: String,
  message: String,
  result: Option[Set[EsContractInfo]]
) extends EsResponse[Option[Set[EsContractInfo]]]

object EsContractInfoResponse {
  implicit val decoder: Decoder[EsContractInfoResponse] = deriveDecoder
}
