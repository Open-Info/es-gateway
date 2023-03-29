package com.verify.esg.neo.model

import com.verify.esg.model.{EthAddress, EthAddressId, EthContract}
import neotypes.QueryArgMapper
import neotypes.generic.semiauto.{deriveCaseClassArgMapper, deriveProductResultMapper}
import neotypes.mappers.ResultMapper

final case class Address(ethAddressId: EthAddressId, isContract: Boolean)
object Address {
  def fromEthAddress(ethAddress: EthAddress): Address =
    Address(
      ethAddressId = ethAddress.addressId,
      isContract = ethAddress.isInstanceOf[EthContract]
    )

  implicit val queryArgMapper: QueryArgMapper[Address] = deriveCaseClassArgMapper
  implicit val resultMapper: ResultMapper[Address] = deriveProductResultMapper
}
