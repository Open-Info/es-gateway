package com.verify.esg.model

import com.verify.esg.client.etherscan.model.{EsContractInfo, EsTransaction}
import io.circe.Encoder
import io.circe.generic.semiauto.deriveEncoder

final case class EthTransaction(
  hash: String,
  to: EthAddress,
  from: EthAddress,
  value: TransactionValue
)

object EthTransaction {
  def build(esTransaction: EsTransaction, contractInfo: Set[EsContractInfo]): Option[EthTransaction] = {
    val to =
      esTransaction.to
        .map { to =>
          if (contractInfo.exists(_.contractAddress == to)) EthContract(to)
          else EthWallet(to)
        }
        .orElse(esTransaction.contractAddress.map(EthContract(_)))

    val from =
      if (contractInfo.exists(_.contractAddress == esTransaction.from)) EthContract(esTransaction.from)
      else EthWallet(esTransaction.from)

    to.map { to =>
      EthTransaction(
        hash = esTransaction.hash,
        to = to,
        from = from,
        value = esTransaction.value
      )
    }
  }

  implicit val encoder: Encoder[EthTransaction] = deriveEncoder
}
