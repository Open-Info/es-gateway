package com.verify.esg.client

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import cats.syntax.option._
import com.verify.common.ras
import com.verify.esg.EsClientConfig
import com.verify.esg.client.etherscan.EsClient
import com.verify.esg.client.etherscan.model.EsTransaction
import com.verify.esg.model.EthAddressId.EthAddressOps
import com.verify.esg.model.{DeserializationError, EthAddressId, SttpError, TransactionValue, HttpError => HttpDomainError}
import org.scalatest.EitherValues
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should._
import sttp.client3.httpclient.cats.HttpClientCatsBackend
import sttp.client3.{Request, UriContext}
import sttp.model.{Method, StatusCode}

class EsClientSpec extends AnyFlatSpec with Matchers with EitherValues {
  val esResponse: String = ras("es-response.json").get

  val config: EsClientConfig =
    EsClientConfig(
      uri = uri"https://api.etherscan.io/api",
      apiKey = "some-key"
    )

  val walletId: EthAddressId = "0x0e9989e703f39880a8e2759bb93b4a9ddd11accf".unsafeEth

  def goodTransactionsRequest(request: Request[_, _]): Boolean =
    (request.method is Method.GET) &&
      (request.uri.scheme contains "https") &&
      (request.uri.host contains "api.etherscan.io") &&
      (request.uri.paramsSeq == Seq(
        ("module", "account"),
        ("action", "txlist"),
        ("address", walletId),
        ("startblock", "0"),
        ("endblock", "1"),
        ("sort", "desc"),
        ("apikey", "some-key"))) &&
      (request.uri.path == Seq("api"))

  behavior of "getTransactions"

  it should "not generate an error on successful requests" in {
    val backend = HttpClientCatsBackend.stub[IO]
      .whenRequestMatches(goodTransactionsRequest)
      .thenRespond(esResponse)

    val esClient = EsClient[IO](config, backend)
    val result = esClient.getTransactions(walletId, 0, 1).unsafeRunSync()

    val expected =
      Set(
        EsTransaction(
          hash = "0x80d527379ae8940ca3dc15042e73f16b25446a90336824b5a24c3d34c5dfd41a",
          timeStamp = 1617630210L,
          to = "0x0e9989e703f39880a8e2759bb93b4a9ddd11accf".unsafeEth.some,
          from = "0x6dba2793e1b0e47fdab2a5156c90a05033726bdd".unsafeEth,
          value = TransactionValue(124197120000000000L.toString),
          contractAddress = None
        ),
        EsTransaction(
          hash = "0xb4b37733664ba5257877942a7e683ce0282fcf37165ee075d476a01fcc4f74ef",
          timeStamp = 1617742136L,
          to = "0x0e9989e703f39880a8e2759bb93b4a9ddd11accf".unsafeEth.some,
          from = "0x6dba2793e1b0e47fdab2a5156c90a05033726bdd".unsafeEth,
          value = TransactionValue(13191840000000000L.toString),
          contractAddress = None
        ),
        EsTransaction(
          hash = "0x53a76601f5a7417267a0d5ae3d948127bfa86ec8ed784443ac8e9d6b08baedf2",
          timeStamp = 1618925739L,
          to = "0xe0b32c2e7fd602fd47e64c319d00e3cbbad31ea3".unsafeEth.some,
          from = "0x0e9989e703f39880a8e2759bb93b4a9ddd11accf".unsafeEth,
          value = TransactionValue(131592960000000000L.toString),
          contractAddress = None
        )
      )

    result shouldBe expected
  }

  it should "return a deserialization error if the partner sends bad information" in {
    val backend = HttpClientCatsBackend.stub[IO]
      .whenRequestMatches(goodTransactionsRequest)
      .thenRespond("""{ "foo": "bar" }""")

    val esClient = EsClient[IO](config, backend)
    val result = esClient.getTransactions(walletId, 0, 1).attempt.unsafeRunSync()
    val error = result.left.value

    error shouldBe a[DeserializationError]
  }

  it should "return an sttp error if the request fails on send/receive" in {
    val backend = HttpClientCatsBackend.stub[IO]
      .whenRequestMatches(goodTransactionsRequest)
      .thenRespondF(IO.raiseError(new Throwable("Boom!")))

    val esClient = EsClient[IO](config, backend)
    val result = esClient.getTransactions(walletId, 0, 1).attempt.unsafeRunSync()
    val error = result.left.value

    error shouldBe a[SttpError]
  }

  it should "return an HTTP error for all HTTP error codes" in {
    val backend = HttpClientCatsBackend.stub[IO]
      .whenRequestMatches(goodTransactionsRequest)
      .thenRespond("You ran into an error!", StatusCode.InternalServerError)

    val esClient = EsClient[IO](config, backend)
    val result = esClient.getTransactions(walletId, 0, 1).attempt.unsafeRunSync()
    val error = result.left.value

    error shouldBe a[HttpDomainError]
  }
}
