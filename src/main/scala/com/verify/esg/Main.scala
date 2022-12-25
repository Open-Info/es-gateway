package com.verify.esg

import cats.effect._
import cats.implicits._
import com.verify.esg.client.EsClientImpl
import com.verify.esg.service.EsServiceImpl
import sttp.client3.httpclient.cats.HttpClientCatsBackend

object Main extends IOApp {
  private val walletId: String = "0x0e9989e703F39880A8E2759BB93B4A9dDD11aCcF"

  override def run(args: List[String]): IO[ExitCode] = {
    HttpClientCatsBackend.resource[IO]().use { implicit backend =>
      for {
        config <- EsgConfig.load[IO]
        esClient = new EsClientImpl[IO](config.esClientConfig)
        esService = new EsServiceImpl[IO](esClient)
        _ <- esService.getFriends(walletId).flatMap(_.toVector.traverse(IO.println))
      } yield ExitCode.Success
    }
  }
}
