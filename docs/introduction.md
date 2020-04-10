# Introduction

WIP

Notes:

ADTs (products and coproducts)

Type classes (our own and heavily depend on type classes from modules such as Cats) e.g. encoding/decoding

Type level programming (Shapeless) where even well typed ADTs can be a form of type level programming as the compiler can tell us if something is wrong with our code with "well typed types" e.g.

userName: UserName, password: Password

whereas the compiler cannot help us with

userName: String, password: String

constraining/narrowing types is a good thing.

Avoid use of Scala Futures in favour of Cats IO as Futures are non-deterministic and not a true Monad

Streaming (FS2) - Work with streams functionally; describe without running, resource management including acquiring and releases resources.

Docker - run everything in Docker including tests (isolation is a good thing)