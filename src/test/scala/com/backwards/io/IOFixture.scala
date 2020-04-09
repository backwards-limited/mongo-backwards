package com.backwards.io

import scala.concurrent.ExecutionContext
import cats.effect.{ConcurrentEffect, ContextShift, IO}

trait IOFixture {
  implicit val contextShift: ContextShift[IO] = IO.contextShift(ExecutionContext.global)

  implicit val concurrentEffect: ConcurrentEffect[IO] = IO.ioConcurrentEffect(contextShift)
}