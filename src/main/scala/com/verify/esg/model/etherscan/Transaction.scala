package com.verify.esg.model.etherscan

import io.circe.Codec
import io.circe.generic.semiauto.deriveCodec
import monix.newtypes.NewtypeWrapped
import monix.newtypes.integrations.DerivedCirceCodec

final case class Transaction(
  hash: String,
  to: String,
  from: String,
  value: Transaction.Value
)

object Transaction {
  type Value = Value.Type
  object Value extends NewtypeWrapped[BigInt] with DerivedCirceCodec

  implicit val codec: Codec[Transaction] = deriveCodec
}
