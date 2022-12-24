package com.verify.esg.model.etherscan

final case class Transaction(
  hash: String,
  to: String,
  from: String,
  value: String
)
