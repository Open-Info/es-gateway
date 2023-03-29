package com.verify.esg.model

import io.circe.Encoder
import io.circe.generic.semiauto.deriveEncoder
import io.circe.syntax.EncoderOps

sealed trait EthAddress {
  val addressId: EthAddressId
}

object EthAddress {
  implicit val encoder: Encoder[EthAddress] =
    Encoder.instance {
      case e: EthContract => e.asJson
      case e: EthWallet   => e.asJson
    }
}

final case class EthWallet(addressId: EthAddressId) extends EthAddress
object EthWallet {
  implicit val encoder: Encoder[EthWallet] = deriveEncoder
}

final case class EthContract(addressId: EthAddressId) extends EthAddress
object EthContract {
  implicit val encoder: Encoder[EthContract] = deriveEncoder
}
