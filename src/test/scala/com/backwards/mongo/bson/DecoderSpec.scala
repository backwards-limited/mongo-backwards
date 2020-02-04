package com.backwards.mongo.bson

import org.bson.BsonValue
import org.mongodb.scala.bson.BsonDocument
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec

class DecoderSpec extends AnyWordSpec with Matchers {
  "BSON" should {
    "be decoded to ADT" in {
      val user = User("id", "Bob", 42)
      val bsonValue = BsonDocument("id" -> user.id, "name" -> user.name, "age" -> user.age)

      println(Decoder[User].decode(bsonValue))
    }
  }
}