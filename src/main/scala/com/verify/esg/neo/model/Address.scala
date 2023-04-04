package com.verify.esg.neo.model

import com.verify.esg.model.{EthAddress, EthAddressId}
import neotypes.QueryArgMapper
import neotypes.generic.semiauto.{deriveCaseClassArgMapper, deriveProductResultMapper}
import neotypes.mappers.ResultMapper

final case class Address(`type`: EthAddress.Type, ethAddressId: EthAddressId)
object Address {
  def wallet(ethAddressId: EthAddressId): Address = Address(EthAddress.Type.Wallet, ethAddressId)
  def contract(ethAddressId: EthAddressId): Address = Address(EthAddress.Type.Contract, ethAddressId)
  def unknown(ethAddressId: EthAddressId): Address = Address(EthAddress.Type.Unknown, ethAddressId)

  def fromEthAddress(ethAddress: EthAddress): Address =
    ethAddress match {
      case EthAddress.Contract(ethAddressId) => contract(ethAddressId)
      case EthAddress.Wallet(ethAddressId) => wallet(ethAddressId)
      case EthAddress.Unknown(ethAddressId) => unknown(ethAddressId)
    }

  implicit val queryArgMapper: QueryArgMapper[Address] = deriveCaseClassArgMapper
  implicit val resultMapper: ResultMapper[Address] = deriveProductResultMapper
}
