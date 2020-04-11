package com.backwards.mongo

import cats.effect.{ConcurrentEffect, IO}
import cats.implicits._
import fs2.Stream
import fs2.interop.reactivestreams._
import org.mongodb.scala.bson.BsonDocument
import org.mongodb.scala.result.InsertOneResult
import org.scalacheck.Gen
import com.mongodb.reactivestreams.client.MongoCollection
import com.backwards.mongo.bson.Encoder.ops._

trait MongoFixture extends UserFixture {
  def init(mongo: Stream[IO, Mongo])(implicit C: ConcurrentEffect[IO]): Stream[IO, Mongo] =
    mongo flatMap { implicit mongo =>
      mongo.database().listCollectionNames().toStream[IO].flatMap { collectionName =>
        mongo.collection(collectionName, classOf[BsonDocument]).deleteMany(BsonDocument()).toStream[IO]
      }
    } through(_ => mongo)

  def seedUsers(mongo: Stream[IO, Mongo])(implicit C: ConcurrentEffect[IO]): Stream[IO, Mongo] = {
    mongo evalTap { implicit mongo =>
      val collection: MongoCollection[BsonDocument] = mongo.collection("users", classOf[BsonDocument])

      Gen.listOfN(10, genUser).sample.fold(IO.raiseError[Int](new Exception("Odd! No User data to seed")))(seedUsers(collection))
    }
  }

  def seedUsers(collection: MongoCollection[BsonDocument])(users: List[User]): IO[Int] =
    users traverse { user =>
      IO(collection.insertOne(user.asDocument).subscribe(NoOpsSubscriber[InsertOneResult]))
    } map(_.size)
}