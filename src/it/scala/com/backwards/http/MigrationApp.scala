package com.backwards.http

import cats.effect.{ExitCode, IO, IOApp}
import cats.implicits._
import io.circe.generic.auto._
import io.circe.syntax._
import fs2._
import pureconfig.generic.auto._
import sttp.client._
import sttp.client.asynchttpclient.WebSocketHandler
import sttp.client.circe._
import com.backwards.config.PureConfig.config
import com.backwards.http.Sttp._
import com.backwards.mongo.Mongo.{mongo, _}
import com.backwards.mongo._

// TODO - App shutsdown when using sync version of Sttp Backend but not using async???
object MigrationApp extends IOApp with MongoFixture {
  def run(args: List[String]): IO[ExitCode] =
    program(
      seedUsers(init(mongo(config[MongoConfig]("mongo")))),
      asyncHttpClientCatsBackend
    ).compile.drain.as(ExitCode.Success)

  def program(mongo: Stream[IO, Mongo], backend: Stream[IO, SttpBackend[IO, Nothing, WebSocketHandler]]): Stream[IO, Response[String Either String]] =
    for {
      implicit0(backend: SttpBackend[IO, Nothing, WebSocketHandler]) <- backend
      implicit0(mongo: Mongo) <- mongo
      response <- users.evalMap(_.fold(IO.raiseError, processUser))
    } yield {
      scribe info s"$response"
      response
    }

  def processUser(implicit backend: SttpBackend[IO, Nothing, WebSocketHandler]): User => IO[Response[String Either String]] =
    user => basicRequest
      .body(user.asJson)
      .post(uri"https://httpbin.org/post")
      .send()
}