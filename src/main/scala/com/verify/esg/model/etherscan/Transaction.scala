package com.verify.esg.model.etherscan

import io.circe.Encoder
import io.circe.generic.semiauto.deriveEncoder

final case class Transaction(
  hash: String,
  to: String,
  from: String,
  value: String
)

object Transaction {
  implicit val encoder: Encoder[Transaction] = deriveEncoder
}
