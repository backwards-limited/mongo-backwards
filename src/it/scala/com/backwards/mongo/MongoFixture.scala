package com.backwards.mongo

import cats.effect.{ConcurrentEffect, IO}
import cats.implicits._
import fs2.Stream
import fs2.interop.reactivestreams._
import org.mongodb.scala.bson.BsonDocument
import org.scalacheck.Gen
import com.mongodb.reactivestreams.client.MongoCollection
import com.backwards.mongo.bson.Encoder.ops._

trait MongoFixture extends UserFixture {
  def init(mongo: Stream[IO, Mongo])(implicit C: ConcurrentEffect[IO]): Stream[IO, Mongo] =
    mongo flatTap { implicit mongo =>
      mongo.database().listCollectionNames().toStream[IO].flatMap { collectionName =>
        mongo.collection(collectionName, classOf[BsonDocument]).deleteMany(BsonDocument()).toStream[IO]
      }
    }

  def seedUsers(mongo: Stream[IO, Mongo])(implicit C: ConcurrentEffect[IO]): Stream[IO, Mongo] =
    mongo flatTap { implicit mongo =>
      implicit val collection: MongoCollection[BsonDocument] = mongo.collection("users", classOf[BsonDocument])

      Stream.eval(IO.fromEither(Gen.listOfN(10, genUser).sample.toRight(new Exception("Odd! No user test data")))).flatMap(seedUsers)
    }

  def seedUsers(users: List[User])(implicit collection: MongoCollection[BsonDocument], C: ConcurrentEffect[IO]): Stream[IO, Int] =
    users traverse { user =>
      collection.insertOne(user.asDocument).toStream[IO]
    } map(_.size)
}