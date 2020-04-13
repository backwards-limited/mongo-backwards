package com.backwards.http

import java.nio.ByteBuffer
import java.util.UUID
import cats.effect.{ExitCode, IO, IOApp}
import cats.implicits._
import io.circe.generic.auto._
import fs2._
import fs2.kafka._
import pureconfig.generic.auto._
import com.backwards.config.PureConfig.config
import com.backwards.kafka.Kafka.kafkaProducer
import com.backwards.kafka.circe.Serializer._
import com.backwards.mongo.Mongo.mongo
import com.backwards.mongo._
import sttp.client._
import sttp.client.asynchttpclient.WebSocketHandler
import sttp.client.asynchttpclient.fs2.AsyncHttpClientFs2Backend

/**
 * Check Kafka i.e. have events been published (brew install kafkacat):
 *
 * kafkacat -L -b localhost:9092
 *
 * kafkacat -C -b localhost:9092 -t users -o beginning
 */
object MongoToEndpointMigrationApp extends IOApp with MongoFixture {
  val v: IO[SttpBackend[IO, Stream[IO, ByteBuffer], WebSocketHandler]] = AsyncHttpClientFs2Backend[IO]()

  def run(args: List[String]): IO[ExitCode] =
    AsyncHttpClientFs2Backend[IO]().flatMap { implicit backend =>
      program(
        seedUsers(init(mongo(config[MongoConfig]("mongo"))))
      ).compile.drain
    }.as(ExitCode.Success)



  def program(mongo: Stream[IO, Mongo])(implicit backend: SttpBackend[IO, Stream[IO, ByteBuffer], NothingT]): Stream[IO, /*Response*/ Unit] = {
    MongoMigration.run(mongo, process)

    /*kafkaProducer flatMap { kafka =>
      MongoMigration.run(mongo, process(kafka))
    }*/
  }

  /*def run(args: List[String]): IO[ExitCode] =
    program(
      seedUsers(init(mongo(config[MongoConfig]("mongo")))),
      kafkaProducer[UUID, User](config[KafkaConfig]("kafka"))
    ).compile.drain.as(ExitCode.Success)

  def program(mongo: Stream[IO, Mongo], kafkaProducer: Stream[IO, KafkaProducer[IO, UUID, User]]): Stream[IO, ProducerResult[UUID, User, Unit]] =
    kafkaProducer flatMap { kafka =>
      MongoMigration.run(mongo, process(kafka))
    }

  def process(kafkaProducer: KafkaProducer[IO, UUID, User]): User => Stream[IO, ProducerResult[UUID, User, Unit]] =
    user => Stream.eval {
      val record = ProducerRecord("users", user.id, user)

      kafkaProducer.produce(ProducerRecords.one(record)).flatten.map { producerResult =>
        scribe.info(producerResult.toString)
        producerResult
      }
    }*/

  def process(implicit backend: SttpBackend[IO, Stream[IO, ByteBuffer], NothingT]): User => Stream[IO, /*Response*/ Unit] =
    user => {
      println(s"===> TODO - Use given user")

      val stream: Stream[IO, ByteBuffer] = Stream.emits(List("Hello, ".getBytes, "world".getBytes)).map(ByteBuffer.wrap)

      Stream.emit {
        basicRequest
          .streamBody(stream)
          .post(uri"https://httpbin.org/post")
          .send()
          .map { response => println(s"RECEIVED:\n${response.body}") }
      }

      /*basicRequest
        .body("I want a stream!")
        .post(uri"https://httpbin.org/post")
        .response(asStreamAlways[Stream[IO, ByteBuffer]])
        .send()
        .flatMap { response =>
          response.body
            .map(bb => Chunk.array(bb.array))
            .through(text.utf8DecodeC)
            .compile
            .foldMonoid
        }
        .map { body => println(s"RECEIVED:\n$body") }*/
    }
}

/*

def subscribe[F[_], S](topic: String, clientId: String)(implicit backend: SttpBackend[F, S, NothingT]): F[Response[String Either String]] =
    basicRequest.get(uri"http://localhost:9094/internal/client/$clientId/subscribe").send()

================

val clientId = "client-id"

implicit val backend: SttpBackendStub[Identity, Nothing] = SttpBackendStub.synchronous
  .whenRequestMatchesPartial({
    case r if r.method == Method.GET && r.uri.path.startsWith(List("internal", "client", clientId, "subscribe")) =>
      Response("", StatusCode.NoContent)
  })

val restClient = RestClient(RestClientConfig("localhost:9094"))

val v: Identity[Response[String Either String]] = restClient.subscribe("topic", clientId)

println(s"===> $v")

 */