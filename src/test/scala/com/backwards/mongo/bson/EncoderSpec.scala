package com.backwards.mongo.bson

import org.bson.BsonValue
import org.mongodb.scala.bson.BsonDocument
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec

class EncoderSpec extends AnyWordSpec with Matchers {
  "ADT" should {
    "be encoded to BSON" in {
      val user = User("id", "Bob", 42)
      val bsonValue: BsonValue = Encoder[User].encode(user)

      bsonValue mustBe BsonDocument("id" -> user.id, "name" -> user.name, "age" -> user.age)
    }
  }
}

final case class User(id: String, name: String, age: Int)