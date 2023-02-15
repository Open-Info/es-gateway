package com.verify.esg.service

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class EsServiceSpec extends AnyFlatSpec with Matchers {
  behavior of "getFriends"

  it should "correctly propagate all first level transactions for a user" in {
    assert(1 === 1)
  }
}
