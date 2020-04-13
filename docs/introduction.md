# Introduction

Streaming with Scala - A PoC showing fundamental Scala FP (functional programming) with emphasis on streaming using examples from [Mongo](https://mongodb.github.io/mongo-java-driver-reactivestreams/1.13/) to [Cassandra](https://github.com/datastax/java-driver/tree/4.x/manual/core), [Kafka](https://fd4s.github.io/fs2-kafka/) and [HTTP endpoints](https://sttp.softwaremill.com/en/latest/).

The following concepts are covered:

- ADT (Algebraic Data Types)
- Type classes (custom as well as depending on modules such as [Cats](https://typelevel.org/cats/))
- Type level programming specifically with [Shapeless](https://github.com/milessabin/shapeless)
- Streaming with [FS2](https://fs2.io/)
- [Docker](https://www.docker.com/) and [Docker Compose](https://docs.docker.com/compose/) with emphasis on running tests within Docker



WIP - TODO - Remove the following

Notess

Type level programming (Shapeless) where even well typed ADTs can be a form of type level programming as the compiler can tell us if something is wrong with our code with "well typed types" e.g.

userName: UserName, password: Password

whereas the compiler cannot help us with

userName: String, password: String

constraining/narrowing types is a good thing.

Avoid use of Scala Futures in favour of Cats IO as Futures are non-deterministic and not a true Monad

Streaming (FS2) - Work with streams functionally; describe without running, resource management including acquiring and releases resources.

Docker - run everything in Docker including tests (isolation is a good thing)