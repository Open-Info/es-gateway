package com.verify.esg.http

import org.scalatest.flatspec.AnyFlatSpec

class WalletRouteSpec extends AnyFlatSpec {
  behavior of "GET /wallet/{walletId}/friend"

  it should "exist and be happy" in {
    assert(1 === 1)
  }
}
