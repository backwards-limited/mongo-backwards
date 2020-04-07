package com.backwards.mongo.bson

import java.util.UUID
import cats.implicits._
import shapeless._
import shapeless.labelled.{FieldType, field}
import org.mongodb.scala.bson.BsonValue

abstract class Decoder[A] {
  def decode(bsonValue: BsonValue): Throwable Either A
}

object Decoder extends Decoders {
  def apply[A: Decoder]: Decoder[A] = implicitly
}

abstract class Decoders extends LowerPriorityDecoders {
  implicit val hnilDecoder: Decoder[HNil] =
    _ => HNil.asRight[Throwable]

  implicit def hlistDecoder[K <: Symbol, H, T <: HList](
    implicit W: Witness.Aux[K],
    HDecoder: Lazy[Decoder[H]],
    TDecoder: Lazy[Decoder[T]]
  ): Decoder[FieldType[K, H] :: T] = { bsonValue =>
    for {
      bsonDocument <- Either.catchNonFatal(bsonValue.asDocument())
      headName = W.value.name
      headBsonValue <- Either.catchNonFatal(bsonDocument.get(headName))
      head <- HDecoder.value.decode(headBsonValue)
      _ = bsonDocument.remove(headName)
      tail <- TDecoder.value.decode(bsonDocument)
    } yield field[K](head) :: tail
  }

  implicit def genDecoder[A, H <: HList](
    implicit G: LabelledGeneric.Aux[A, H],
    D: Lazy[Decoder[H]]
  ): Decoder[A] = { bsonValue =>
    D.value.decode(bsonValue).map(G.from)
  }
}

trait LowerPriorityDecoders {
  this: Decoders =>

  implicit val stringDecoder: Decoder[String] =
    bsonValue => Either.catchNonFatal(bsonValue.asString.getValue)

  implicit val intDecoder: Decoder[Int] =
    bsonValue => Either.catchNonFatal(bsonValue.asNumber.intValue)

  implicit val uuidDecoder: Decoder[UUID] =
    bsonValue => Either.catchNonFatal(UUID.fromString(bsonValue.asString.getValue))
}