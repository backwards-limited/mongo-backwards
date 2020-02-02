package com.backwards.mongo.bson

import shapeless.labelled.FieldType
import shapeless.{::, HList, HNil, LabelledGeneric, Lazy, Witness}
import org.bson.BsonValue
import org.mongodb.scala.bson.{BsonDocument, BsonNumber, BsonString}

abstract class Encoder[A] {
  def encode(a: A): BsonValue
}

object Encoder extends Implicits {
  def apply[A: Encoder]: Encoder[A] = implicitly[Encoder[A]]
}

abstract class Implicits {
  implicit val stringEncoder: Encoder[String] =
    BsonString(_)

  implicit val intEncoder: Encoder[Int] =
    BsonNumber(_)

  implicit val hnilEncoder: Encoder[HNil] =
    _ => BsonDocument()

  implicit def hlistEncoder[K <: Symbol, H, T <: HList](
    implicit W: Witness.Aux[K],
    HEncoder: Lazy[Encoder[H]],
    TEncoder: Lazy[Encoder[T]]
  ): Encoder[FieldType[K, H] :: T] = { case h :: t =>
    val name = W.value.name
    val head = HEncoder.value.encode(h)
    val tail: BsonValue = TEncoder.value.encode(t)

    // TODO - We lose the order (which is fine for JSON, but would be nice to preserve)
    tail.asDocument().append(name, head)
  }

  implicit def genEncoder[A, H <: HList](
    implicit Gen: LabelledGeneric.Aux[A, H],
    Encoder: Lazy[Encoder[H]]
  ): Encoder[A] = { a =>
    Encoder.value.encode(Gen.to(a))
  }
}