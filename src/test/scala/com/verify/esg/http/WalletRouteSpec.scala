package com.verify.esg.http

import cats.data.Kleisli
import cats.effect.IO
import cats.effect.unsafe.implicits.global
import com.verify.esg.model.EthAddressId.EthAddressOps
import com.verify.esg.model.{EthAddress, EthAddressId, EthTransaction, TransactionValue}
import com.verify.esg.service.EsService
import io.circe.{Decoder, Json, parser}
import org.http4s._
import org.http4s.circe.CirceEntityCodec.circeEntityDecoder
import org.scalamock.scalatest.MockFactory
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import java.time.Instant

class WalletRouteSpec extends AnyFlatSpec with Matchers with MockFactory {
  val walletId: EthAddressId = "0x0e9989e703f39880a8e2759bb93b4a9ddd11accf".unsafeEth

  val walletIds: Set[EthAddressId] =
    Set(
      "0x6dba2793e1b0e47fdab2a5156c90a05033726bdd",
      "0xe0b32c2e7fd602fd47e64c319d00e3cbbad31ea3"
    ).map(_.unsafeEth)

  val transactions: Set[EthTransaction] =
    Set(
      EthTransaction(
        hash = "0x80d527379ae8940ca3dc15042e73f16b25446a90336824b5a24c3d34c5dfd41a",
        timestamp = Instant.ofEpochSecond(1654646411L),
        to = EthAddress.Wallet("0x0e9989e703f39880a8e2759bb93b4a9ddd11accf".unsafeEth),
        from = EthAddress.Wallet("0x6dba2793e1b0e47fdab2a5156c90a05033726bdd".unsafeEth),
        value = TransactionValue(124197120000000000L.toString)
      ),
      EthTransaction(
        hash = "0xb4b37733664ba5257877942a7e683ce0282fcf37165ee075d476a01fcc4f74ef",
        timestamp = Instant.ofEpochSecond(1654646411L),
        to = EthAddress.Wallet("0x0e9989e703f39880a8e2759bb93b4a9ddd11accf".unsafeEth),
        from = EthAddress.Wallet("0x6dba2793e1b0e47fdab2a5156c90a05033726bdd".unsafeEth),
        value = TransactionValue(13191840000000000L.toString)
      ),
      EthTransaction(
        hash = "0x53a76601f5a7417267a0d5ae3d948127bfa86ec8ed784443ac8e9d6b08baedf2",
        timestamp = Instant.ofEpochSecond(1654646411L),
        to = EthAddress.Wallet("0xe0b32c2e7fd602fd47e64c319d00e3cbbad31ea3".unsafeEth),
        from = EthAddress.Wallet("0x0e9989e703f39880a8e2759bb93b4a9ddd11accf".unsafeEth),
        value = TransactionValue(131592960000000000L.toString)
      )
    )

  def runRequest[A: Decoder](
    routes: Kleisli[IO, Request[IO], Response[IO]],
    request: Request[IO]
  )(implicit ed: EntityDecoder[IO, A]): IO[(Int, A)] =
    for {
      response <- routes.run(request)
      body <- response.as[A]
    } yield (response.status.code, body)

  def buildFriendUri(walletId: EthAddressId): Uri =
    Uri(path = Uri.Path.unsafeFromString(s"/wallet/${walletId.value}/friend"))

  def buildTransactionUri(walletId: EthAddressId): Uri =
    Uri(path = Uri.Path.unsafeFromString(s"/wallet/${walletId.value}/transaction"))

  behavior of "GET /wallet/{walletId}/friend"

  it should "return the correct encoded response" in {
    val mockEsService = mock[EsService[IO]]
    (mockEsService.getFriends _)
      .expects(walletId)
      .returns(IO.pure(walletIds))

    val walletRoute = WalletRoute[IO](mockEsService).apply.orNotFound
    val request = Request[IO](Method.GET, buildFriendUri(walletId))

    val expectedString =
      """
        |[
        |  "0x6dba2793e1b0e47fdab2a5156c90a05033726bdd",
        |  "0xe0b32c2e7fd602fd47e64c319d00e3cbbad31ea3"
        |]
        |""".stripMargin
    val expected = parser.parse(expectedString).getOrElse(Json.Null)

    val (code, body) = runRequest[Json](walletRoute, request).unsafeRunSync()
    code shouldBe 200
    body shouldBe expected
  }

  behavior of "GET /wallet/{walletId}/transaction"

  it should "return the correct encoded response" in {
    val mockEsService = mock[EsService[IO]]
    (mockEsService.getTransactions _)
      .expects(walletId)
      .returns(IO.pure(transactions))

    val walletRoute = WalletRoute[IO](mockEsService).apply.orNotFound
    val request = Request[IO](Method.GET, buildTransactionUri(walletId))

    val expectedString =
      """
        |[
        |  {
        |    "hash" : "0x80d527379ae8940ca3dc15042e73f16b25446a90336824b5a24c3d34c5dfd41a",
        |    "timestamp" : "2022-06-08T00:00:11Z",
        |    "to" : {
        |      "type" : "wallet",
        |      "addressId" : "0x0e9989e703f39880a8e2759bb93b4a9ddd11accf"
        |    },
        |    "from" : {
        |      "type" : "wallet",
        |      "addressId" : "0x6dba2793e1b0e47fdab2a5156c90a05033726bdd"
        |    },
        |    "value" : "124197120000000000"
        |  },
        |  {
        |    "hash" : "0xb4b37733664ba5257877942a7e683ce0282fcf37165ee075d476a01fcc4f74ef",
        |    "timestamp" : "2022-06-08T00:00:11Z",
        |    "to" : {
        |      "type" : "wallet",
        |      "addressId" : "0x0e9989e703f39880a8e2759bb93b4a9ddd11accf"
        |    },
        |    "from" : {
        |      "type" : "wallet",
        |      "addressId" : "0x6dba2793e1b0e47fdab2a5156c90a05033726bdd"
        |    },
        |    "value" : "13191840000000000"
        |  },
        |  {
        |    "hash" : "0x53a76601f5a7417267a0d5ae3d948127bfa86ec8ed784443ac8e9d6b08baedf2",
        |    "timestamp" : "2022-06-08T00:00:11Z",
        |    "to" : {
        |      "type" : "wallet",
        |      "addressId" : "0xe0b32c2e7fd602fd47e64c319d00e3cbbad31ea3"
        |    },
        |    "from" : {
        |      "type" : "wallet",
        |      "addressId" : "0x0e9989e703f39880a8e2759bb93b4a9ddd11accf"
        |    },
        |    "value" : "131592960000000000"
        |  }
        |]
        |""".stripMargin
    val expected = parser.parse(expectedString).getOrElse(Json.Null)

    val (code, body) = runRequest[Json](walletRoute, request).unsafeRunSync()

    code shouldBe 200
    body shouldBe expected
  }
}
