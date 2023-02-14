package com.verify.esg.client

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import com.verify.esg.EsClientConfig
import com.verify.esg.model.{DeserializationError, SttpError, HttpError => HttpDomainError}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should._
import sttp.client3.httpclient.cats.HttpClientCatsBackend
import sttp.client3.{Request, UriContext}
import sttp.model.{Method, StatusCode}

import scala.io.Source
import scala.util.Using

class EsClientSpec extends AnyFlatSpec with Matchers {
  val esResponse: String = Using(Source.fromResource("es-response.json")) { source =>
    source.getLines.mkString.filterNot(_.isWhitespace)
  }.getOrElse(fail("Could not read esResponse from resources"))

  def goodTransactionsRequest(request: Request[_, _]): Boolean =
    (request.method is Method.GET) &&
      (request.uri.scheme contains "https") &&
      (request.uri.host contains "api.etherscan.io") &&
      (request.uri.paramsSeq == Seq(
        ("module", "account"),
        ("action", "txlist"),
        ("address", "some-id"),
        ("apikey", "some-key"))) &&
      (request.uri.path == Seq("api"))

  behavior of "getTransactions"

  it should "not generate an error on successful requests" in {
    val backend = HttpClientCatsBackend.stub[IO]
      .whenRequestMatches(goodTransactionsRequest)
      .thenRespond(esResponse)

    val config = EsClientConfig(
      uri = uri"https://api.etherscan.io/api",
      apiKey = "some-key"
    )

    val esClient = new EsClientImpl[IO](config, backend)
    val result = esClient.getTransactions("some-id").unsafeRunSync()

    result shouldBe a[Right[_, _]]
  }

  it should "return a deserialization error if the partner sends bad information" in {
    val backend = HttpClientCatsBackend.stub[IO]
      .whenRequestMatches(goodTransactionsRequest)
      .thenRespond("""{ "foo": "bar" }""")

    val config = EsClientConfig(
      uri = uri"https://api.etherscan.io/api",
      apiKey = "some-key"
    )

    val esClient = new EsClientImpl[IO](config, backend)
    val result = esClient.getTransactions("some-id").unsafeRunSync()
    val error = result.left.getOrElse(fail())

    error shouldBe a[DeserializationError]
  }

  it should "return an sttp error if the request fails on send/receive" in {
    val backend = HttpClientCatsBackend.stub[IO]
      .whenRequestMatches(goodTransactionsRequest)
      .thenRespondF(IO.raiseError(new Throwable("Boom!")))

    val config = EsClientConfig(
      uri = uri"https://api.etherscan.io/api",
      apiKey = "some-key"
    )

    val esClient = new EsClientImpl[IO](config, backend)
    val result = esClient.getTransactions("some-id").unsafeRunSync()
    val error = result.left.getOrElse(fail())

    error shouldBe a[SttpError]
  }

  it should "return an HTTP error for all HTTP error codes" in {
    val backend = HttpClientCatsBackend.stub[IO]
      .whenRequestMatches(goodTransactionsRequest)
      .thenRespond("You ran into an error!", StatusCode.InternalServerError)

    val config = EsClientConfig(
      uri = uri"https://api.etherscan.io/api",
      apiKey = "some-key"
    )

    val esClient = new EsClientImpl[IO](config, backend)
    val result = esClient.getTransactions("some-id").unsafeRunSync()
    val error = result.left.getOrElse(fail())

    error shouldBe a[HttpDomainError]
  }
}
