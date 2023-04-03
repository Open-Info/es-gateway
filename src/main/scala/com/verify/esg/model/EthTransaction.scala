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
  def build(esTransaction: EsTransaction, contracts: Set[EthAddressId]): Option[EthTransaction] = {
    val to =
      esTransaction.to
        .map(t => EthAddress(t, contracts.contains(t)))
        .orElse(esTransaction.contractAddress.map(EthContract(_)))

    val from = EthAddress(esTransaction.from, contracts.contains(esTransaction.from))

    to.map { to =>
      EthTransaction(
        hash = esTransaction.hash,
        timestamp = Instant.ofEpochSecond(esTransaction.timeStamp),
        to = to,
        from = from,
        value = esTransaction.value
      )
    }
  }

  implicit val encoder: Encoder[EthTransaction] = deriveEncoder
}
