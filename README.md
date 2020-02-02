# Scala Mongo by Backwards

Scala module providing Mongodb functionality.

Of course you can simply clone/fork this module.
However, this module is also published on [JitPack](https://jitpack.io), where the following dependency can be added to your project:

```scala
lazy val backwards: Seq[ModuleID] = {
  val version = "<version>"

  Seq(
    "com.github.backwards-limited" % "mongo-backwards" % version % Test classifier "tests"
  ) ++ Seq(
    "com.github.backwards-limited" % "mongo-backwards" % version
  )
}
```

The above dependency declaration includes source, test and integration test code.

You will also need to add a **resolver** to **JitPack**:

```scala
resolvers ++= Seq(
  <your current resolvers>,
  "jitpack" at "https://jitpack.io"
)
```

## Table of Contents

- [Setup](docs/setup.md)

- [SBT](docs/sbt.md)

- [Release](docs/release.md)