# Setup

Apologies I shall only cover **Mac** - One day I may include Linux and Windows.

Install [Homebrew](https://brew.sh) for easy package management on Mac:

```bash
ruby -e "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/master/install)"
```

Required installations:

```bash
$ brew cask install java
$ brew install jenv
$ brew install scala
$ brew install sbt
```

These install the necessary Java and Scala environment, where [jenv](http://www.jenv.be) manages said environment.

To configure our JVM environment:

```bash
$ jenv versions
    system
  * 14 (set by /Users/davidainslie/workspace/backwards/mongo-backwards/.java-version)
```

From the available versions (if not yet set) set the latest:

```bash
$ jenv local 14
```

Finally [setup Docker](https://hub.docker.com/editions/community/docker-ce-desktop-mac/).