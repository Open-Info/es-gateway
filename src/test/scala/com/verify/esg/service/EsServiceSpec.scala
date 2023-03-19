package com.verify.esg.service

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import com.verify.esg.client.EsClient
import com.verify.esg.model.etherscan.{Transaction, TransactionsResponse}
import org.scalamock.scalatest.MockFactory
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class EsServiceSpec extends AnyFlatSpec with Matchers with MockFactory {
  val walletId: String = "0x0e9989e703f39880a8e2759bb93b4a9ddd11accf"

  val transactionsResponse: TransactionsResponse =
    TransactionsResponse(
      status = "1",
      message = "OK",
      result = Vector(
        Transaction(
          hash = "0x80d527379ae8940ca3dc15042e73f16b25446a90336824b5a24c3d34c5dfd41a",
          to = "0x0e9989e703f39880a8e2759bb93b4a9ddd11accf",
          from = "0x6dba2793e1b0e47fdab2a5156c90a05033726bdd",
          value = Transaction.Value(BigInt(124197120000000000L))
        ),
        Transaction(
          hash = "0xb4b37733664ba5257877942a7e683ce0282fcf37165ee075d476a01fcc4f74ef",
          to = "0x0e9989e703f39880a8e2759bb93b4a9ddd11accf",
          from = "0x6dba2793e1b0e47fdab2a5156c90a05033726bdd",
          value = Transaction.Value(BigInt(13191840000000000L))
        ),
        Transaction(
          hash = "0x53a76601f5a7417267a0d5ae3d948127bfa86ec8ed784443ac8e9d6b08baedf2",
          to = "0xe0b32c2e7fd602fd47e64c319d00e3cbbad31ea3",
          from = "0x0e9989e703f39880a8e2759bb93b4a9ddd11accf",
          value = Transaction.Value(BigInt(131592960000000000L))
        )
      )
    )

  behavior of "getFriends"

  it should "return all first level interactions of a given WalletId" in {
    val mockEsClient = mock[EsClient[IO]]
    (mockEsClient.getTransactions _)
      .expects(walletId)
      .returns(IO.pure(transactionsResponse))

    val esService = EsService[IO](mockEsClient)
    val expected = Set(
      "0x6dba2793e1b0e47fdab2a5156c90a05033726bdd",
      "0xe0b32c2e7fd602fd47e64c319d00e3cbbad31ea3"
    )
    val result = esService.getFriends(walletId).unsafeRunSync()

    result shouldBe expected
  }

it should "return an empty Set if no transactions are found" in {
    val mockEsClient = mock[EsClient[IO]]
    (mockEsClient.getTransactions _)
      .expects(walletId)
      .returns(IO.pure(transactionsResponse.copy(result = Vector.empty)))

    val esService = EsService[IO](mockEsClient)
    val result = esService.getFriends(walletId).unsafeRunSync()

    result shouldBe empty
  }

  behavior of "getTransactions"

  it should "correctly extract the results from the TransactionsResponse" in {
    val mockEsClient = mock[EsClient[IO]]
    (mockEsClient.getTransactions _)
      .expects(walletId)
      .returns(IO.pure(transactionsResponse))

    val esService = EsService[IO](mockEsClient)
    val result = esService.getTransactions(walletId).unsafeRunSync()

    result shouldBe transactionsResponse.result
  }
}
