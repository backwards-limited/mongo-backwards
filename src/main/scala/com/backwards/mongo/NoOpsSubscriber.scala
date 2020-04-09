package com.backwards.mongo

import org.reactivestreams.{Subscriber, Subscription}

class NoOpsSubscriber[R] extends Subscriber[R] {
  def onSubscribe(s: Subscription): Unit = s.request(1)

  def onNext(t: R): Unit = ()

  def onError(t: Throwable): Unit = ()

  def onComplete(): Unit = ()
}

object NoOpsSubscriber {
  def apply[R] = new NoOpsSubscriber[R]
}