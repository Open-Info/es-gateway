package com.verify.esg

import cats.effect._
import com.verify.esg.client.etherscan.EsClient
import com.verify.esg.service.EsService
import sttp.client3.SttpBackend
import sttp.client3.httpclient.cats.HttpClientCatsBackend

object Main extends IOApp {
  override def run(args: List[String]): IO[ExitCode] =
    resources.use { sttpBackend =>
      AppConfig
        .load[IO]
        .flatMap { config =>
          val esClient = EsClient[IO](config.esClient, sttpBackend)
          val esService = EsService[IO](esClient, config.esService)

          Server
            .build[IO](esService, config.httpServer.host, config.httpServer.port)
            .use(_ => IO.never)
            .as(ExitCode.Success)
        }
    }

  private def resources: Resource[IO, SttpBackend[IO, Any]] = HttpClientCatsBackend.resource[IO]()
}
