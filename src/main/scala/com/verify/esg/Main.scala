package com.verify.esg

import cats.effect._
import cats.implicits._
import com.verify.esg.client.EsClientImpl
import sttp.client3.httpclient.cats.HttpClientCatsBackend

object Main extends IOApp {
  private val walletId: String = "0x0e9989e703F39880A8E2759BB93B4A9dDD11aCcF"

  override def run(args: List[String]): IO[ExitCode] = {
    HttpClientCatsBackend.resource[IO]().use { implicit backend =>
      for {
        config <- EsgConfig.load[IO]
        esClient = new EsClientImpl[IO](config.esClientConfig)
        response <- esClient.getTransactions(walletId)
        _ <- response match {
          case Right(response) =>
            val wallets = response.result.foldLeft(Set.empty[String])((acc, t) => acc + t.to + t.from)
            val finalWallets = (wallets - walletId).toVector
            finalWallets.traverse(IO.println)
          case Left(e) =>
            IO.raiseError(e.ex)
        }
      } yield ExitCode.Success
    }
  }
}
