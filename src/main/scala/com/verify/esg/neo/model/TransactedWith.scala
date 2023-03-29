package com.verify.esg.neo.model

import com.verify.esg.model.{EthTransaction, TransactionValue}
import neotypes.QueryArgMapper
import neotypes.generic.semiauto._
import neotypes.mappers.ResultMapper

final case class TransactedWith(
  hash: String,
  value: TransactionValue
)

object TransactedWith {
  def fromEthTransaction(esTransaction: EthTransaction): TransactedWith =
    TransactedWith(
      hash = esTransaction.hash,
      value = esTransaction.value
    )

  implicit val queryArgMapper: QueryArgMapper[TransactedWith] = deriveCaseClassArgMapper
  implicit val resultMapper: ResultMapper[TransactedWith] = deriveProductResultMapper
}