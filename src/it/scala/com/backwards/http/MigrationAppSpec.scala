package com.backwards.http

import java.util.UUID
import cats.effect.IO
import cats.implicits._
import sttp.client.asynchttpclient.cats.AsyncHttpClientCatsBackend
import sttp.client.{NothingT, SttpBackend, _}
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import com.backwards.http.MigrationApp._
import com.backwards.io.IOFixture
import com.backwards.mongo.{MongoFixture, User}

class MigrationAppSpec extends AnyWordSpec with Matchers with IOFixture with MongoFixture {
  "App" should {
    "process user" in {
      val dummyResult = """{ "ok": true }"""

      implicit val backend: SttpBackend[IO, Nothing, NothingT] = AsyncHttpClientCatsBackend.stub[IO]
        .whenRequestMatches(_.uri.path.startsWith(List("post")))
        .thenRespond(dummyResult)

      val user = User(UUID.randomUUID, "Bob", "Boo", "bob@gmail.com")

      val response: Response[String Either String] = process(user).unsafeRunSync

      response.body mustBe dummyResult.asRight
    }
  }
}