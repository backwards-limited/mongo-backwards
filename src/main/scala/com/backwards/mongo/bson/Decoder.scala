package com.backwards.mongo.bson

import org.mongodb.scala.bson.BsonValue
import cats.implicits._
import shapeless.labelled.{FieldType, field}
import shapeless._

abstract class Decoder[A] {
  def decode(bsonValue: BsonValue): Throwable Either A
}

object Decoder extends DecoderImplicits {
  def apply[A: Decoder]: Decoder[A] = implicitly
}

abstract class DecoderImplicits {
  implicit val stringDecoder: Decoder[String] =
    bsonValue => Either.catchNonFatal(bsonValue.asString.getValue)

  implicit val intDecoder: Decoder[Int] =
    bsonValue => Either.catchNonFatal(bsonValue.asNumber.intValue)

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
    implicit Gen: LabelledGeneric.Aux[A, H],
    Decoder: Lazy[Decoder[H]]
  ): Decoder[A] = { bsonValue =>
    Decoder.value.decode(bsonValue).map(Gen.from)
  }
}