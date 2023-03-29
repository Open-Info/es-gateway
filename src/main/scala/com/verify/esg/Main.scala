package com.verify.esg

import cats.effect._
import com.verify.esg.client.etherscan.EsClient
import com.verify.esg.neo.TransactionNeo
import com.verify.esg.service.EsService
import neotypes.cats.effect.implicits._
import neotypes.{Driver, GraphDatabase}
import org.neo4j.driver.AuthTokens
import sttp.client3.SttpBackend
import sttp.client3.httpclient.cats.HttpClientCatsBackend

object Main extends IOApp {
  override def run(args: List[String]): IO[ExitCode] =
    resources.use { case (sttpBackend, driver) =>
      for {
        config <- AppConfig.load[IO]
        esClient = EsClient[IO](config.esClientConfig, sttpBackend)
        transactionNeo = TransactionNeo[IO](driver)
        esService = EsService[IO](esClient, transactionNeo, config.esServiceConfig)
        _ <- Server.build[IO](esService, config.serverConfig.host, config.serverConfig.port).use(_ => IO.never)
      } yield ExitCode.Success
    }

  private def resources: Resource[IO, (SttpBackend[IO, Any], Driver[IO])] =
    for {
      backend <- HttpClientCatsBackend.resource[IO]()
      driver <- GraphDatabase.driver[IO]("bolt://localhost:7687", AuthTokens.basic("neo4j", "password"))
    } yield (backend, driver)
}
