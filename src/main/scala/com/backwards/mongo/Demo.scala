package com.backwards.mongo

import cats.effect.{ExitCode, IO, IOApp}
import fs2.interop.reactivestreams._
import org.bson.Document
import org.mongodb.scala.result.InsertOneResult
import org.reactivestreams.{Subscriber, Subscription}
import com.mongodb.reactivestreams.client.{MongoCollection, MongoDatabase}

/**
 * brew services start mongodb-community
 */
object Demo extends IOApp {
  /*val doc = new Document("name", "MongoDB")
    .append("type", "database")
    .append("count", 1)
    .append("info", new Document("x", 203).append("y", 102))

  val allDocuments: Stream[IO, Document] =
    for {
      conn <- Stream.resource(Mongo.fromUrl[IO]("mongodb://localhost"))
      database = conn.getDatabase("test_db")
      collection = database.getCollection("test_collection")
      _ = collection.insertOne(doc, new SingleResultCallback[Void] {
        def onResult(result: Void, t: Throwable): Unit = println("===> callback")
      })
      document <- collection.find().stream[IO]
    } yield {
      println(s"===> Got doc: $document")
      document
    }

  println(allDocuments.take(5).compile.toList.unsafeRunSync)*/

  def run(args: List[String]): IO[ExitCode] = {
    val doc = new Document("name", "MongoDB")
      .append("type", "database")
      .append("count", 1)
      .append("info", new Document("x", 203).append("y", 102))

    /*val allDocuments: Stream[IO, Document] =
      for {
        conn <- Stream.resource(Mongo.fromUrl[IO]("mongodb://localhost"))
        database = conn.getDatabase("test_db")
        collection = database.getCollection("test_collection")
        _ = collection.insertOne(doc, new SingleResultCallback[Void] {
          def onResult(result: Void, t: Throwable): Unit = println("===> callback")
        })
        document <- collection.find().stream[IO]
      } yield {
        println(s"===> Got doc: $document")
        document
      }

    allDocuments.compile.toList.map { docs =>
      ExitCode(0)
    }*/

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


    val collection: MongoCollection[Document] = database.getCollection("test_collection")

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

/*
def run(args: List[String]): IO[ExitCode] = {
    val weather =
      Vector.tabulate (100) { t =>
        Weather("London",
          Instant ofEpochMilli t,
          temp = 19.0 + math.random(),
          humidity = 50.0 + math.random()
        )
      }

    val program =
      for { cli <- Client[IO](ConnectionString("mongodb://localhost"))
            db = cli.database("test")
            coll = db.collection("weather")
            _ <- coll.insert(weather)
            day <- coll.find[Weather]()
            } yield day

    program
      .compile
      .toVector
      .attempt
      .map { d =>
        println("List of warm days:\n" + d)
        ExitCode(0)
      }
  }
 */
