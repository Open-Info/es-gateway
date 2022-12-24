package com.verify.esg.model.etherscan

final case class TransactionsResponse(
  status: String,
  message: String,
  result: Vector[Transaction]
)
