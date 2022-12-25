package com.verify.esg

import cats.effect._
import com.verify.esg.client.EsClientImpl
import com.verify.esg.service.EsServiceImpl
import sttp.client3.httpclient.cats.HttpClientCatsBackend

object Main extends IOApp {
  override def run(args: List[String]): IO[ExitCode] = {
    HttpClientCatsBackend.resource[IO]().use { implicit backend =>
      for {
        config <- EsgConfig.load[IO]
        esClient = new EsClientImpl[IO](config.esClientConfig)
        esService = new EsServiceImpl[IO](esClient)
        _ <- Server.build[IO](esService).use(_ => IO.never)
      } yield ExitCode.Success
    }
  }
}
