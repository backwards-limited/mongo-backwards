package com.backwards.mongo

import org.reactivestreams.{Subscriber, Subscription}

class NoOpsSubscriber[R] extends Subscriber[R] {
  def onSubscribe(s: Subscription): Unit = {
    scribe.info()
    s.request(1)
  }

  def onNext(t: R): Unit = scribe.info()

  def onError(t: Throwable): Unit = scribe.info()

  def onComplete(): Unit = scribe.info()
}

object NoOpsSubscriber {
  def apply[R] = new NoOpsSubscriber[R]
}