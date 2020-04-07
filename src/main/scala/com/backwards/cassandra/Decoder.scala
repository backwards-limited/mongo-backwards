package com.backwards.cassandra

import java.util.UUID
import scala.jdk.CollectionConverters._
import scala.reflect.{ClassTag, _}
import cats.implicits._
import shapeless._
import shapeless.labelled.{FieldType, field}
import com.datastax.oss.driver.api.core.cql.{ColumnDefinition, Row}

abstract class Decoder[A] {
  def decode(row: Row, fieldName: Option[String] = None): Throwable Either A
}

object Decoder extends Decoders {
  def apply[A: Decoder]: Decoder[A] = implicitly
}

abstract class Decoders extends LowerPriorityDecoders {
  implicit val hnilDecoder: Decoder[HNil] =
    (_, _) => HNil.asRight[Throwable]

  implicit def hlistDecoder[K <: Symbol, H, T <: HList](
    implicit W: Witness.Aux[K],
    HDecoder: Lazy[Decoder[H]],
    TDecoder: Lazy[Decoder[T]]
  ): Decoder[FieldType[K, H] :: T] = {
    case (row, Some(fieldName)) =>
      // TODO - Should this be an error, or can we ignore this case?
      ???
    case (row, _) =>
      for {
        head <- HDecoder.value.decode(row, Option(W.value.name))
        tail <- TDecoder.value.decode(row)
      } yield field[K](head) :: tail
  }

  implicit def genDecoder[A, H <: HList](
    implicit G: LabelledGeneric.Aux[A, H],
    D: Lazy[Decoder[H]]
  ): Decoder[A] =
    (row, _) => D.value.decode(row).map(G.from)

  def decoder[A: ClassTag]: Decoder[A] = {
    case (row, Some(fieldName)) =>
      row.get(fieldName, classTag[A].runtimeClass).asInstanceOf[A].asRight[Throwable]
    case (row, _) =>
      new Exception(s"""Failed to extract field of type "${classTag[A].runtimeClass}" from given row: ${columnDefinitions(row).mkString(", ")}""").asLeft[A]
  }

  def columnDefinitions(row: Row): List[ColumnDefinition] =
    row.getColumnDefinitions.iterator().asScala.toList
}

trait LowerPriorityDecoders {
  this: Decoders =>

  implicit val stringDecoder: Decoder[String] = decoder[String]

  implicit val intDecoder: Decoder[Int] = decoder[Int]

  implicit val uuidDecoder: Decoder[UUID] = decoder[UUID]
}