package com.verify.esg

import cats.syntax.all._
import com.verify.esg.neo.DerivedNeoMappers
import monix.newtypes.integrations.DerivedCirceCodec
import monix.newtypes.{BuildFailure, NewtypeValidated, NewtypeWrapped}

import scala.util.matching.Regex

package object model {
  type EthAddressId = EthAddressId.Type
  object EthAddressId extends NewtypeValidated[String] with DerivedCirceCodec with DerivedNeoMappers {
    private val regex: Regex = "^0x[0-9a-fA-F]{40}$".r

    override def apply(value: String): Either[BuildFailure[Type], Type] =
      regex
        .findFirstIn(value)
        .as(unsafe(value))
        .toRight(BuildFailure(s"'$value'"))

    implicit class EthAddressOps(val ethString: String) extends AnyVal {
      def unsafeEth: EthAddressId = unsafe(ethString)
    }

    def fromString(ethString: String): Option[EthAddressId] = regex.findFirstIn(ethString).flatMap(apply(_).toOption)
  }

  type TransactionValue = TransactionValue.Type
  object TransactionValue extends NewtypeWrapped[String] with DerivedCirceCodec with DerivedNeoMappers
}
