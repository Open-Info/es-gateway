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
  private def build(to: Option[EthAddress], from: EthAddress, esTransaction: EsTransaction): Option[EthTransaction] =
    to.map { to =>
      EthTransaction(
        hash = esTransaction.hash,
        timestamp = Instant.ofEpochSecond(esTransaction.timeStamp),
        to = to,
        from = from,
        value = esTransaction.value
      )
    }

  // build EthTransaction from EsTransaction based on a known set of contracts
  def build(esTransaction: EsTransaction, contracts: Option[Set[EthAddressId]]): Option[EthTransaction] = {
    val to =
      esTransaction.to
        .map(t => EthAddress(t, contracts.map(_.contains(t))))
        .orElse(esTransaction.contractAddress.map(EthAddress.Contract))

    val from = EthAddress(esTransaction.from, contracts.map(_.contains(esTransaction.from)))

    build(to, from, esTransaction)
  }

  implicit val encoder: Encoder[EthTransaction] = deriveEncoder
}
