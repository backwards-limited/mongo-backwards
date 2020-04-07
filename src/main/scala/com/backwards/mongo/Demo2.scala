package com.backwards.mongo

import java.util.UUID
import cats.effect.{ExitCode, IO, IOApp}
import fs2.interop.reactivestreams._
import org.bson.{BsonValue, Document}
import org.mongodb.scala.bson.BsonDocument
import org.mongodb.scala.result.InsertOneResult
import org.reactivestreams.{Subscriber, Subscription}
import com.mongodb.reactivestreams.client.{MongoCollection, MongoDatabase}
import com.backwards.cassandra.User
import com.backwards.mongo.bson.Encoder

/**
 * brew services start mongodb-community
 */
object Demo2 extends IOApp {
  def run(args: List[String]): IO[ExitCode] = {
    import com.mongodb.reactivestreams.client.MongoClients

    // val mongoClient = MongoClients.create

    // Use a Connection String
    val mongoClient = MongoClients.create("mongodb://localhost")

    // or a Connection String
    //val mongoClient = MongoClients.create(new ConnectionString("mongodb://localhost"))

    // or provide custom MongoClientSettings
   /* val clusterSettings: ClusterSettings = ClusterSettings.builder.hosts(List(new ServerAddress("localhost")).asJava).build
    val settings: MongoClientSettings = MongoClientSettings.builder() .clusterSettings(clusterSettings).build
    val mongoClient = MongoClients.create(settings)*/


    val database: MongoDatabase = mongoClient.getDatabase("test_db")


    val collection: MongoCollection[BsonDocument] = database.getCollection("test_collection", classOf[BsonDocument])

    val user = User(UUID.randomUUID(), "Bob", "Boo", "bob@gmail.com")

    val doc = Encoder[User].encode(user).asDocument()

    collection.insertOne(doc).subscribe(new Subscriber[InsertOneResult] {
      def onSubscribe(s: Subscription): Unit = {
        println("onSubscribe")
        s.request(1)
      }

      def onNext(t: InsertOneResult): Unit = println("onNext")

      def onError(t: Throwable): Unit = println("onError")

      def onComplete(): Unit = println("onComplete")
    })


    val allDocuments = for {
      document <- collection.find().toStream[IO]
    } yield {
      println(s"===> Got doc: $document")
      document
    }

    allDocuments.compile.toList.map { docs =>
      println(s"Final docs = $docs")
      ExitCode(0)
    }
  }
}