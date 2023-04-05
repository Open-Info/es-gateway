package com.verify.esg.model

import com.verify.esg.client.etherscan.model.EsTransaction
import io.circe.Encoder
import io.circe.generic.semiauto.deriveEncoder

import java.time.Instant

final case class EthTransaction(
  hash: String,
  timestamp: Instant,
  to: EthAddress,
  from: EthAddress,
  value: TransactionValue
)

object EthTransaction {
  private def build(to: EthAddress, from: EthAddress, esTransaction: EsTransaction): EthTransaction =
      EthTransaction(
        hash = esTransaction.hash,
        timestamp = Instant.ofEpochSecond(esTransaction.timeStamp),
        to = to,
        from = from,
        value = esTransaction.value
      )

  // build EthTransaction from EsTransaction based on an optional known set of contracts
  def build(esTransaction: EsTransaction, contracts: Option[Set[EthAddressId]]): Option[EthTransaction] = {
    val to =
      esTransaction.to
        .map(t => EthAddress(t, contracts.map(_.contains(t))))
        .orElse(esTransaction.contractAddress.map(EthAddress.Contract))

    val from = EthAddress(esTransaction.from, contracts.map(_.contains(esTransaction.from)))

    to.map(build(_, from, esTransaction))
  }

  implicit val encoder: Encoder[EthTransaction] = deriveEncoder
}
