package com.verify.esg.service

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import cats.syntax.option._
import com.verify.esg.EsServiceConfig
import com.verify.esg.client.etherscan.EsClient
import com.verify.esg.client.etherscan.model.EsTransaction
import com.verify.esg.model.EthAddressId.EthAddressOps
import com.verify.esg.model.{EthAddressId, EthTransaction, EthWallet, TransactionValue}
import com.verify.esg.neo.TransactionNeo
import org.scalamock.scalatest.MockFactory
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class EsServiceSpec extends AnyFlatSpec with Matchers with MockFactory {
  val config: EsServiceConfig = EsServiceConfig(numBlocks = 100)

  val walletId: EthAddressId = "0x0e9989e703f39880a8e2759bb93b4a9ddd11accf".unsafeEth

  val transactions: Vector[EsTransaction] =
    Vector(
      EsTransaction(
        hash = "0x80d527379ae8940ca3dc15042e73f16b25446a90336824b5a24c3d34c5dfd41a",
        to = "0x0e9989e703f39880a8e2759bb93b4a9ddd11accf".unsafeEth.some,
        from = "0x6dba2793e1b0e47fdab2a5156c90a05033726bdd".unsafeEth,
        value = TransactionValue(124197120000000000L.toString),
        contractAddress = None
      ),
      EsTransaction(
        hash = "0xb4b37733664ba5257877942a7e683ce0282fcf37165ee075d476a01fcc4f74ef",
        to = "0x0e9989e703f39880a8e2759bb93b4a9ddd11accf".unsafeEth.some,
        from = "0x6dba2793e1b0e47fdab2a5156c90a05033726bdd".unsafeEth,
        value = TransactionValue(13191840000000000L.toString),
        contractAddress = None
      ),
      EsTransaction(
        hash = "0x53a76601f5a7417267a0d5ae3d948127bfa86ec8ed784443ac8e9d6b08baedf2",
        to = "0xe0b32c2e7fd602fd47e64c319d00e3cbbad31ea3".unsafeEth.some,
        from = "0x0e9989e703f39880a8e2759bb93b4a9ddd11accf".unsafeEth,
        value = TransactionValue(131592960000000000L.toString),
        contractAddress = None
      )
    )

  behavior of "getFriends"

  it should "return all first level interactions of a given WalletId" in {
    val mockTransactionNeo: TransactionNeo[IO] = mock[TransactionNeo[IO]]
    (mockTransactionNeo.pushTransactions _).expects(*).returning(IO.unit)

    val mockEsClient = mock[EsClient[IO]]
    (mockEsClient.getTransactions _)
      .expects(walletId, 0, 100)
      .returns(IO.pure(transactions))
    (mockEsClient.getLastBlock _)
      .expects()
      .returns(IO.pure(100))
    (mockEsClient.getContractInfo _)
      .expects(*)
      .returns(IO.pure(Set.empty))

    val esService = EsService[IO](mockEsClient, mockTransactionNeo, config)
    val expected = Set(
      "0x6dba2793e1b0e47fdab2a5156c90a05033726bdd",
      "0xe0b32c2e7fd602fd47e64c319d00e3cbbad31ea3"
    )
    val result = esService.getFriends(walletId).unsafeRunSync()

    result shouldBe expected
  }

  it should "return an empty Set if no transactions are found" in {
    val mockTransactionNeo: TransactionNeo[IO] = mock[TransactionNeo[IO]]
    (mockTransactionNeo.pushTransactions _).expects(*).returning(IO.unit)

    val mockEsClient = mock[EsClient[IO]]
    (mockEsClient.getTransactions _)
      .expects(walletId, 0, 100)
      .returns(IO.pure(Vector.empty))
    (mockEsClient.getLastBlock _)
      .expects()
      .returns(IO.pure(100))
    (mockEsClient.getContractInfo _)
      .expects(*)
      .returns(IO.pure(Set.empty))

    val esService = EsService[IO](mockEsClient, mockTransactionNeo, config)
    val result = esService.getFriends(walletId).unsafeRunSync()

    result shouldBe empty
  }

  behavior of "getTransactions"

  it should "correctly extract the results from the EsTransactionsResponse" in {
    val mockTransactionNeo: TransactionNeo[IO] = mock[TransactionNeo[IO]]
    (mockTransactionNeo.pushTransactions _).expects(*).returning(IO.unit)

    val mockEsClient = mock[EsClient[IO]]
    (mockEsClient.getTransactions _)
      .expects(walletId, 0, 100)
      .returns(IO.pure(transactions))
    (mockEsClient.getLastBlock _)
      .expects()
      .returns(IO.pure(100))
    (mockEsClient.getContractInfo _)
      .expects(*)
      .returns(IO.pure(Set.empty))

    val esService = EsService[IO](mockEsClient, mockTransactionNeo, config)
    val result = esService.getTransactions(walletId).unsafeRunSync()
    val expected =
      Vector(
        EthTransaction(
          hash = "0x80d527379ae8940ca3dc15042e73f16b25446a90336824b5a24c3d34c5dfd41a",
          to = EthWallet("0x0e9989e703f39880a8e2759bb93b4a9ddd11accf".unsafeEth),
          from = EthWallet("0x6dba2793e1b0e47fdab2a5156c90a05033726bdd".unsafeEth),
          value = TransactionValue(124197120000000000L.toString)
        ),
        EthTransaction(
          hash = "0xb4b37733664ba5257877942a7e683ce0282fcf37165ee075d476a01fcc4f74ef",
          to = EthWallet("0x0e9989e703f39880a8e2759bb93b4a9ddd11accf".unsafeEth),
          from = EthWallet("0x6dba2793e1b0e47fdab2a5156c90a05033726bdd".unsafeEth),
          value = TransactionValue(13191840000000000L.toString)
        ),
        EthTransaction(
          hash = "0x53a76601f5a7417267a0d5ae3d948127bfa86ec8ed784443ac8e9d6b08baedf2",
          to = EthWallet("0xe0b32c2e7fd602fd47e64c319d00e3cbbad31ea3".unsafeEth),
          from = EthWallet("0x0e9989e703f39880a8e2759bb93b4a9ddd11accf".unsafeEth),
          value = TransactionValue(131592960000000000L.toString)
        )
      )

    result shouldBe expected
  }
}
