package com.verify.esg.client.etherscan.model

import com.verify.esg.model.{EthAddressId, TransactionValue}
import io.circe.Decoder
import io.circe.generic.semiauto.deriveDecoder

final case class EsTransaction(
  hash: String,
  to: Option[EthAddressId],
  from: EthAddressId,
  value: TransactionValue,
  contractAddress: Option[EthAddressId]
)

object EsTransaction {
  implicit val maybeAddressDecoder: Decoder[Option[EthAddressId]] = {
    val emptyStringDecoder =
      Decoder.decodeString.map { s =>
        Option.when(s.nonEmpty)(EthAddressId.fromString(s)).flatten
      }

    emptyStringDecoder or Decoder.decodeOption[EthAddressId]
  }

  implicit val decoder: Decoder[EsTransaction] = deriveDecoder
}
