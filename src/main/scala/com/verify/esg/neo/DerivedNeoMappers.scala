package com.verify.esg.neo

import cats.syntax.all._
import monix.newtypes.{HasBuilder, HasExtractor}
import neotypes.mappers.{ParameterMapper, ValueMapper}
import org.neo4j.driver.Value

trait DerivedNeoMappers extends DerivedNeoValueMapper with DerivedNeoParameterMapper

trait DerivedNeoValueMapper {
  implicit def valueMapper[T, S](implicit
    builder: HasBuilder.Aux[T, S],
    valueMapper: ValueMapper[S]
  ): ValueMapper[T] = valueMap[T, S]

  protected def valueMap[T, S](
    fieldName: String,
    value: Option[Value]
  )(implicit
    builder: HasBuilder.Aux[T, S],
    valueMapper: ValueMapper[S]
  ): Either[Throwable, T] =
    valueMapper
      .to(fieldName, value)
      .flatMap(s => builder.build(s).leftMap(_.toException))
}

trait DerivedNeoParameterMapper {
  implicit def parameterMapper[T, S](implicit
    extractor: HasExtractor.Aux[T, S],
    parameterMapper: ParameterMapper[S]
  ): ParameterMapper[T] = parameterMapper.contramap(extractor.extract)
}
