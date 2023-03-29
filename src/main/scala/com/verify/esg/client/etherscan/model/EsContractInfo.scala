package com.verify.esg.client.etherscan.model

import com.verify.esg.model.EthAddressId
import io.circe.Decoder
import io.circe.generic.semiauto.deriveDecoder

final case class EsContractInfo(
  contractAddress: EthAddressId,
  contractCreator: EthAddressId,
  txHash: String
)

object EsContractInfo {
  implicit val decoder: Decoder[EsContractInfo] = deriveDecoder
}
