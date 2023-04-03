package com.verify.esg.neo.model

import com.verify.esg.model.{EthAddress, EthAddressId, EthContract, EthWallet}
import enumeratum.EnumEntry.Lowercase
import enumeratum.{Enum, EnumEntry}
import neotypes.QueryArgMapper
import neotypes.enumeratum.NeotypesEnum
import neotypes.generic.semiauto.{deriveCaseClassArgMapper, deriveProductResultMapper}
import neotypes.mappers.ResultMapper

final case class Address(`type`: Address.Type, ethAddressId: EthAddressId)
object Address {
  def wallet(ethAddressId: EthAddressId): Address = Address(Address.Type.Wallet, ethAddressId)
  def contract(ethAddressId: EthAddressId): Address = Address(Address.Type.Contract, ethAddressId)

  def fromEthAddress(ethAddress: EthAddress): Address =
    ethAddress match {
      case EthContract(ethAddressId) => contract(ethAddressId)
      case EthWallet(ethAddressId) => wallet(ethAddressId)
    }

  sealed trait Type extends EnumEntry with Lowercase
  object Type extends Enum[Type] with NeotypesEnum[Type] {
    case object Wallet extends Type
    case object Contract extends Type

    override val values: IndexedSeq[Type] = findValues
  }

  implicit val queryArgMapper: QueryArgMapper[Address] = deriveCaseClassArgMapper
  implicit val resultMapper: ResultMapper[Address] = deriveProductResultMapper
}
