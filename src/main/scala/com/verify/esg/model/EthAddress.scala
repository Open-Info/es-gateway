package com.verify.esg.model

import enumeratum.EnumEntry.Lowercase
import enumeratum.{CirceEnum, Enum, EnumEntry}
import io.circe.generic.semiauto.deriveEncoder
import io.circe.syntax.EncoderOps
import io.circe.{Encoder, Json}
import neotypes.enumeratum.NeotypesEnum

sealed trait EthAddress {
  val addressId: EthAddressId
  protected val `type`: EthAddress.Type
}

object EthAddress {
  final case class Wallet(addressId: EthAddressId) extends EthAddress {
    override val `type`: EthAddress.Type = EthAddress.Type.Wallet
  }

  final case class Contract(addressId: EthAddressId) extends EthAddress {
    override val `type`: EthAddress.Type = EthAddress.Type.Contract
  }

  final case class Unknown(addressId: EthAddressId) extends EthAddress {
    override val `type`: EthAddress.Type = EthAddress.Type.Unknown
  }

  implicit val contractEncoder: Encoder[Contract] = deriveEncoder
  implicit val walletEncoder: Encoder[Wallet] = deriveEncoder
  implicit val unknownEncoder: Encoder[Unknown] = deriveEncoder

  sealed trait Type extends EnumEntry with Lowercase
  object Type extends Enum[Type] with NeotypesEnum[Type] with CirceEnum[Type] {
    case object Wallet extends Type
    case object Contract extends Type
    case object Unknown extends Type

    override val values: IndexedSeq[Type] = findValues
  }

  def apply(addressId: EthAddressId, isContract: Option[Boolean]): EthAddress =
    isContract
      .map { isContract =>
        if (isContract) Contract(addressId)
        else Wallet(addressId)
      }
      .getOrElse(Unknown(addressId))

  implicit val encoder: Encoder[EthAddress] =
    Encoder.instance { address =>
      val base =
        address match {
          case a: Contract => a.asJson
          case a: Wallet => a.asJson
          case a: Unknown => a.asJson
        }

      base deepMerge Json.obj("type" -> address.`type`.asJson)
    }
}
