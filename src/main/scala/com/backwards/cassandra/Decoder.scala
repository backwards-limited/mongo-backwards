package com.backwards.cassandra

import java.util.UUID
import cats.implicits._
import shapeless.labelled.{FieldType, field}
import shapeless._
import com.datastax.oss.driver.api.core.cql.Row

abstract class Decoder[A] {
  def decode(row: Row, fieldName: Option[String] = None): Throwable Either A
}

object Decoder extends DecoderImplicits {
  def apply[A: Decoder]: Decoder[A] = implicitly
}

abstract class DecoderImplicits {
  implicit val stringDecoder: Decoder[String] = {
    case (row, Some(fieldName)) =>
      row.get(fieldName, classOf[String]).asRight[Throwable]
    case _ =>
      new Exception("TODO").asLeft[String]
  }

  implicit val uuidDecoder: Decoder[UUID] = {
    case (row, Some(fieldName)) =>
      row.get(fieldName, classOf[UUID]).asRight[Throwable]
    case _ =>
      new Exception("TODO").asLeft[UUID]
  }

  implicit val hnilDecoder: Decoder[HNil] =
    (_, _) => HNil.asRight[Throwable]

  implicit def hlistDecoder[K <: Symbol, H, T <: HList](
    implicit W: Witness.Aux[K],
    HDecoder: Lazy[Decoder[H]],
    TDecoder: Lazy[Decoder[T]]
  ): Decoder[FieldType[K, H] :: T] = {
    case (row, Some(fieldName)) =>
      // TODO - Should this be an error?
      ???
    case (row, _) =>
      for {
        h <- HDecoder.value.decode(row, Option(W.value.name))
        t <- TDecoder.value.decode(row)
      } yield field[K](h) :: t
  }

  implicit def genDecoder[A, H <: HList](
    implicit G: LabelledGeneric.Aux[A, H],
    D: Lazy[Decoder[H]]
  ): Decoder[A] =
    (row, _) => D.value.decode(row).map(G.from)
}