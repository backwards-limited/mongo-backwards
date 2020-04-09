package com.backwards.mongo

import cats.effect.IO
import cats.implicits._
import fs2.Stream
import org.mongodb.scala.bson.BsonDocument
import org.mongodb.scala.result.{DeleteResult, InsertOneResult}
import org.scalacheck.Gen
import com.mongodb.reactivestreams.client.{MongoClient, MongoCollection, MongoDatabase}
import com.backwards.mongo.bson.Encoder.ops._

trait MongoFixture extends UserFixture {
  def database(name: String)(implicit mongoClient: MongoClient): IO[MongoDatabase] =
    IO(mongoClient.getDatabase(name))

  def collection(name: String, truncate: Boolean = false)(implicit database: MongoDatabase): IO[MongoCollection[BsonDocument]] =
    IO(database.getCollection(name, classOf[BsonDocument])).flatMap { collection =>
      IO(collection.deleteMany(BsonDocument()).subscribe(NoOpsSubscriber[DeleteResult])).whenA(truncate) *> IO(collection)
    }

  def seed(mongoClient: Stream[IO, MongoClient]): Stream[IO, MongoClient] =
    mongoClient evalTap { implicit mongoClient =>
      for {
        implicit0(database: MongoDatabase) <- database("mydatabase")
        implicit0(collection: MongoCollection[BsonDocument]) <- collection("mycollection", truncate = true)
        updates <- Gen.listOfN(10, genUser).sample.fold(IO.raiseError[Int](new Exception("No User data to seed")))(seedUsers)
      } yield updates
    }

  def seedUsers(users: List[User])(implicit collection: MongoCollection[BsonDocument]): IO[Int] =
    users.traverse { user =>
      IO(collection.insertOne(user.asDocument).subscribe(NoOpsSubscriber[InsertOneResult]))
    }.map(_.size)
}