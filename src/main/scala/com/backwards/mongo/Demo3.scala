package com.backwards.mongo

import java.util.UUID
import scala.concurrent.Future
import cats.effect.{ExitCode, IO, IOApp}
import fs2.interop.reactivestreams._
import org.mongodb.scala.bson.BsonDocument
import org.mongodb.scala.result.InsertOneResult
import org.reactivestreams.{Subscriber, Subscription}
import com.datastax.oss.driver.api.core.{CqlIdentifier, CqlSession}
import com.datastax.oss.driver.api.core.cql.{PreparedStatement, ResultSet, SimpleStatement}
import com.mongodb.reactivestreams.client.{MongoClient, MongoClients, MongoCollection, MongoDatabase}
import com.backwards.cassandra.User
import com.backwards.mongo.bson.Encoder
import fs2._

/**
 * brew services start mongodb-community
 */
object Demo3 extends IOApp {
  def run(args: List[String]): IO[ExitCode] = {

    /////////////////////////////////////////////////////////////////////   MONGO   /////////////////////////////////////////////////////////////////
    val mongoClient: MongoClient = MongoClients.create("mongodb://localhost")

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

    /////////////////////////////////////////////////////////////////////   MONGO   /////////////////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////////////   CASSANDRA   ///////////////////////////////////////////////////////////////

    import scala.jdk.FutureConverters._ // TODO

    implicit class CqlStrings(val context: StringContext) /*extends AnyVal TODO - Put back once pulled up to no longer be a local class */ {
      def cql(args: Any*)(implicit session: CqlSession): Future[PreparedStatement] = {
        val statement = SimpleStatement.newInstance(context.raw(args: _*))
        session.prepareAsync(statement).asScala
      }
    }

    import scala.concurrent.{ExecutionContext, Future} // TODO

    // TODO IO from Future
    def execute(statement: Future[PreparedStatement], params: Any*)(
      implicit executionContext: ExecutionContext, session: CqlSession
    ): Future[ResultSet] =
      statement
        .map(_.bind(params: _*))
        .map(session.execute)

    implicit val session: CqlSession =
      CqlSession
        .builder
        //.addContactPoint(new InetSocketAddress("localhost", 9042))
        //.withLocalDatacenter("DC1")
        .withAuthCredentials("cassandra", "cassandra")
        .withKeyspace(CqlIdentifier.fromCql("mykeyspace"))
        //.addTypeCodecs(TypeCodecs.UUID)
        .build

    import scala.concurrent.ExecutionContext.Implicits.global

    val blah: IO[ResultSet] = IO.fromFuture(IO(execute(cql"insert into mykeyspace.user_by_id (id, email, firstname, lastname) values (?, ?, ?, ?)", user.id, user.email, user.firstName, user.lastName)))

    val xx: Stream[IO, ResultSet] = Stream.eval(blah)

    ///////////////////////////////////////////////////////////////////   CASSANDRA   ///////////////////////////////////////////////////////////////


    val allDocuments = for {
      document <- collection.find().toStream[IO]
      x <- xx
    } yield {
      println(s"===> Got x: $x")


      println(s"===> Got doc: $document")
      document
    }

    allDocuments.compile.toList.map { docs =>
      println(s"Final docs = $docs")
      ExitCode(0)
    }
  }
}