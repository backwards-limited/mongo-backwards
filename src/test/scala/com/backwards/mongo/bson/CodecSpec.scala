package com.backwards.mongo.bson

import java.util.UUID
import cats.implicits._
import org.bson.BsonValue
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import com.backwards.mongo.User

class CodecSpec extends AnyWordSpec with Matchers {
  "ADT" should {
    "be encoded to BSON and decoded back again" in {
      val user = User(UUID.randomUUID, "Bob", "Boo", "bob@gmail.com")

      val bsonValue: BsonValue = Encoder[User].encode(user)

      Decoder[User].decode(bsonValue) mustBe user.asRight[Throwable]
    }
  }
}