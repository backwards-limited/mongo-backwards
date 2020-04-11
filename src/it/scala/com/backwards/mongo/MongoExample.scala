package com.backwards.mongo

import cats.effect.{ExitCode, IO, IOApp}
import fs2.interop.reactivestreams._
import org.bson.Document
import org.mongodb.scala.result.InsertOneResult
import org.reactivestreams.{Subscriber, Subscription}
import com.mongodb.reactivestreams.client.{MongoClients, MongoCollection, MongoDatabase}

/**
 * docker-compose.yml to boot dependent services.
 * However, anything can be used such as Homebrew e.g.
 * brew services start mongodb-community
 */
object MongoExample extends IOApp {
  def run(args: List[String]): IO[ExitCode] = {
    val mongoClient = MongoClients.create("mongodb://localhost")

    val database: MongoDatabase = mongoClient.getDatabase("demo-database")

    val collection: MongoCollection[Document] = database.getCollection("demo-collection")

    val document = new Document("name", "MongoDB")
      .append("type", "database")
      .append("count", 1)
      .append("info", new Document("x", 203).append("y", 102))

    collection.insertOne(document).subscribe(new Subscriber[InsertOneResult] {
      def onSubscribe(s: Subscription): Unit = {
        scribe info s"Persisting document: ${document.toJson}"
        s.request(1)
      }

      def onNext(t: InsertOneResult): Unit = scribe info "onNext"

      def onError(t: Throwable): Unit = scribe info "onError"

      def onComplete(): Unit = scribe info "onComplete"
    })

    val allDocuments = for {
      document <- collection.find().toStream[IO]
    } yield {
      scribe info s"Got document: ${document.toJson}"
      document
    }

    allDocuments.compile.toList.map { documents =>
      scribe info s"Got all documents:\n${documents.map(_.toJson).mkString("\n")}"
      ExitCode(0)
    }
  }
}