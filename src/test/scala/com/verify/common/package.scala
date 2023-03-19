package com.verify

import io.circe.{Decoder, parser}

import scala.io.Source
import scala.util.Using

package object common {
  def ras(path: String): Option[String] =
    Using(Source.fromResource(path)) { source =>
      source.getLines.mkString.filterNot(_.isWhitespace)
    }.toOption

  def rasT[A: Decoder](path: String): Option[A] =
    for {
      rawString <- ras(path)
      json <- parser.parse(rawString).toOption
      result <- json.as[A].toOption
    } yield result
}
