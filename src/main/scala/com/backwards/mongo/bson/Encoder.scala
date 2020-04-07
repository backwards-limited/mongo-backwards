package com.backwards.mongo.bson

import java.util.UUID
import shapeless.labelled.FieldType
import shapeless.{::, HList, HNil, LabelledGeneric, Lazy, Witness}
import org.mongodb.scala.bson.{BsonDocument, BsonNumber, BsonString, BsonValue}

abstract class Encoder[A] {
  def encode(a: A): BsonValue
}

object Encoder extends Encoders {
  def apply[A: Encoder]: Encoder[A] = implicitly
}

abstract class Encoders extends LowerLevelEncoders {
  implicit val hnilEncoder: Encoder[HNil] =
    _ => BsonDocument()

  implicit def hlistEncoder[K <: Symbol, H, T <: HList](
    implicit W: Witness.Aux[K],
    HEncoder: Lazy[Encoder[H]],
    TEncoder: Lazy[Encoder[T]]
  ): Encoder[FieldType[K, H] :: T] = { case h :: t =>
    val headName = W.value.name
    val head = HEncoder.value.encode(h)
    val tail: BsonValue = TEncoder.value.encode(t)

    // TODO - We lose the order (which is fine for JSON, but would be nice to preserve)
    tail.asDocument().append(headName, head)
  }

  implicit def genEncoder[A, H <: HList](
    implicit Gen: LabelledGeneric.Aux[A, H],
    Encoder: Lazy[Encoder[H]]
  ): Encoder[A] = { a =>
    Encoder.value.encode(Gen.to(a))
  }
}

trait LowerLevelEncoders {
  this: Encoders =>

  implicit val stringEncoder: Encoder[String] =
    BsonString.apply

  implicit val intEncoder: Encoder[Int] =
    BsonNumber.apply

  implicit val uuidEncoder: Encoder[UUID] =
    uuid => BsonString(uuid.toString)
}