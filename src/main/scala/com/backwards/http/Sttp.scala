package com.backwards.http

import cats.effect.{ConcurrentEffect, ContextShift, IO}
import fs2.Stream
import sttp.client.asynchttpclient.WebSocketHandler
import sttp.client.asynchttpclient.cats.AsyncHttpClientCatsBackend
import sttp.client.{HttpURLConnectionBackend, Identity, NothingT, SttpBackend}

object Sttp {
  def httpURLConnectionBackend(/*TODO config*/ ): Stream[IO, SttpBackend[Identity, Nothing, NothingT]] = {
    val aquire = IO {
      scribe info s"Acquiring HTTP URL connection backend client"

      HttpURLConnectionBackend()
    }

    val release: SttpBackend[Identity, Nothing, NothingT] => IO[Unit] =
      sttpBackend => IO {
        scribe info "Releasing HTTP URL connection backend client"
        sttpBackend.close()
      }

    Stream.bracket(aquire)(release)
  }

  def asyncHttpClientCatsBackend(/*TODO config*/ implicit C: ConcurrentEffect[IO], CS: ContextShift[IO]): Stream[IO, SttpBackend[IO, Nothing, WebSocketHandler]] = {
    val aquire = {
      scribe info s"Acquiring async HTTP client Cats backend client"
      AsyncHttpClientCatsBackend[IO]()
    }

    val release: SttpBackend[IO, Nothing, WebSocketHandler] => IO[Unit] =
      sttpBackend => {
        scribe info "Releasing async HTTP client Cats backend client"
        sttpBackend.close()
      }

    Stream.bracket(aquire)(release)
  }
}