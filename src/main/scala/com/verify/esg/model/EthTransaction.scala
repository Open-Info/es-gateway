package com.verify.esg.model

import cats.syntax.all._
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
      esTransaction.to match {
        case Some(to) =>
          if (contractInfo.exists(_.contractAddress == to)) EthContract(to).some
          else EthWallet(to).some
        case None => esTransaction.contractAddress.map(EthContract(_))
      }

    val from =
      if (contractInfo.exists(_.contractAddress == esTransaction.from)) EthContract(esTransaction.from).some
      else EthWallet(esTransaction.from).some

    (to, from).mapN { (to, from) =>
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
