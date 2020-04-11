package com.backwards.mongo

import cats.effect.IO
import cats.implicits._
import fs2.Stream
import org.mongodb.scala.bson.BsonDocument
import org.mongodb.scala.result.{DeleteResult, InsertOneResult, UpdateResult}
import org.reactivestreams.{Subscriber, Subscription}
import org.scalacheck.Gen
import com.mongodb.reactivestreams.client.MongoCollection
import com.backwards.mongo.bson.Encoder.ops._
import fs2.interop.reactivestreams._

trait MongoFixture extends UserFixture {
  def mongoCollection(name: String, truncate: Boolean = false)(implicit mongo: Mongo): IO[MongoCollection[BsonDocument]] =
    IO(mongo.collection(name, classOf[BsonDocument])).flatMap { collection =>
      IO(collection.deleteMany(BsonDocument()).subscribe(NoOpsSubscriber[DeleteResult])).whenA(truncate) *> IO(collection)
    }

  def init(mongo: Stream[IO, Mongo]): Stream[IO, Mongo] = {
    mongo evalTap { mongo =>
      IO {
        /*mongo.database().listCollectionNames().subscribe(new Subscriber[UpdateResult] {
          def onSubscribe(s: Subscription): Unit = {
            s.request(1)
          }

          def onNext(t: UpdateResult): Unit = ???

          def onError(t: Throwable): Unit = ???

          def onComplete(): Unit = ???
        })*/
      }
    }
  }

  def seedUsers(mongo: Stream[IO, Mongo]): Stream[IO, Mongo] =
    mongo evalTap { implicit mongo =>
      mongoCollection("users", truncate = true) flatMap { implicit collection =>
        Gen.listOfN(10, genUser).sample.fold(IO.raiseError[Int](new Exception("Odd! No User data to seed")))(seedUsers)
      }
    }

  def seedUsers(users: List[User])(implicit collection: MongoCollection[BsonDocument]): IO[Int] =
    users.traverse { user =>
      IO(collection.insertOne(user.asDocument).subscribe(NoOpsSubscriber[InsertOneResult]))
    }.map(_.size)
}