package com.verify.esg.client.etherscan.model.response

trait EsResponse[A] {
  val status: String
  val message: String
  val result: A
}
