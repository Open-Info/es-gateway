package com.verify.esg.service

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import cats.syntax.option._
import com.verify.esg.EsServiceConfig
import com.verify.esg.client.etherscan.EsClient
import com.verify.esg.client.etherscan.model.EsTransaction
import com.verify.esg.model.EthAddressId.EthAddressOps
import com.verify.esg.model.{EthAddress, EthAddressId, EthTransaction, TransactionValue}
import org.scalamock.scalatest.MockFactory
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import java.time.Instant

class EsServiceSpec extends AnyFlatSpec with Matchers with MockFactory {
  val config: EsServiceConfig = EsServiceConfig(numBlocks = 100)

  val walletId: EthAddressId = "0x0e9989e703f39880a8e2759bb93b4a9ddd11accf".unsafeEth

  val transactions: Set[EsTransaction] =
    Set(
      EsTransaction(
        hash = "0x80d527379ae8940ca3dc15042e73f16b25446a90336824b5a24c3d34c5dfd41a",
        timeStamp = 1654646411L,
        to = "0x0e9989e703f39880a8e2759bb93b4a9ddd11accf".unsafeEth.some,
        from = "0x6dba2793e1b0e47fdab2a5156c90a05033726bdd".unsafeEth,
        value = TransactionValue(124197120000000000L.toString),
        contractAddress = None
      ),
      EsTransaction(
        hash = "0xb4b37733664ba5257877942a7e683ce0282fcf37165ee075d476a01fcc4f74ef",
        timeStamp = 1654646411L,
        to = "0x0e9989e703f39880a8e2759bb93b4a9ddd11accf".unsafeEth.some,
        from = "0x6dba2793e1b0e47fdab2a5156c90a05033726bdd".unsafeEth,
        value = TransactionValue(13191840000000000L.toString),
        contractAddress = None
      ),
      EsTransaction(
        hash = "0x53a76601f5a7417267a0d5ae3d948127bfa86ec8ed784443ac8e9d6b08baedf2",
        timeStamp = 1654646411L,
        to = "0xe0b32c2e7fd602fd47e64c319d00e3cbbad31ea3".unsafeEth.some,
        from = "0x0e9989e703f39880a8e2759bb93b4a9ddd11accf".unsafeEth,
        value = TransactionValue(131592960000000000L.toString),
        contractAddress = None
      )
    )

  behavior of "getFriends"

  it should "return all first level interactions of a given WalletId" in {
    val mockEsClient = mock[EsClient[IO]]
    (mockEsClient.getTransactions: (EthAddressId, Int) => IO[Set[EsTransaction]])
      .expects(walletId, 100)
      .returns(IO.pure(transactions))

    val esService = EsService[IO](mockEsClient, config)
    val expected = Set(
      "0x6dba2793e1b0e47fdab2a5156c90a05033726bdd",
      "0xe0b32c2e7fd602fd47e64c319d00e3cbbad31ea3"
    )
    val result = esService.getFriends(walletId).unsafeRunSync()

    result shouldBe expected
  }

  it should "return an empty Set if no transactions are found" in {
    val mockEsClient = mock[EsClient[IO]]
    (mockEsClient.getTransactions: (EthAddressId, Int) => IO[Set[EsTransaction]])
      .expects(walletId, 100)
      .returns(IO.pure(Set.empty))

    val esService = EsService[IO](mockEsClient, config)
    val result = esService.getFriends(walletId).unsafeRunSync()

    result shouldBe empty
  }

  behavior of "getTransactions"

  it should "correctly extract the results from the EsTransactionsResponse" in {
    val mockEsClient = mock[EsClient[IO]]
    (mockEsClient.getTransactions: (EthAddressId, Int) => IO[Set[EsTransaction]])
      .expects(walletId, 100)
      .returns(IO.pure(transactions))

    val esService = EsService[IO](mockEsClient, config)
    val result = esService.getTransactions(walletId).unsafeRunSync()
    val expected =
      Set(
        EthTransaction(
          hash = "0x80d527379ae8940ca3dc15042e73f16b25446a90336824b5a24c3d34c5dfd41a",
          timestamp = Instant.ofEpochSecond(1654646411L),
          to = EthAddress.Unknown("0x0e9989e703f39880a8e2759bb93b4a9ddd11accf".unsafeEth),
          from = EthAddress.Unknown("0x6dba2793e1b0e47fdab2a5156c90a05033726bdd".unsafeEth),
          value = TransactionValue(124197120000000000L.toString)
        ),
        EthTransaction(
          hash = "0xb4b37733664ba5257877942a7e683ce0282fcf37165ee075d476a01fcc4f74ef",
          timestamp = Instant.ofEpochSecond(1654646411L),
          to = EthAddress.Unknown("0x0e9989e703f39880a8e2759bb93b4a9ddd11accf".unsafeEth),
          from = EthAddress.Unknown("0x6dba2793e1b0e47fdab2a5156c90a05033726bdd".unsafeEth),
          value = TransactionValue(13191840000000000L.toString)
        ),
        EthTransaction(
          hash = "0x53a76601f5a7417267a0d5ae3d948127bfa86ec8ed784443ac8e9d6b08baedf2",
          timestamp = Instant.ofEpochSecond(1654646411L),
          to = EthAddress.Unknown("0xe0b32c2e7fd602fd47e64c319d00e3cbbad31ea3".unsafeEth),
          from = EthAddress.Unknown("0x0e9989e703f39880a8e2759bb93b4a9ddd11accf".unsafeEth),
          value = TransactionValue(131592960000000000L.toString)
        )
      )

    result shouldBe expected
  }
}
