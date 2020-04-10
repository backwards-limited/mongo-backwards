# Execute

## Unit Test

```bash
$ sbt test
```

## Integration Test

There is a [docker-compose file](../docker-compose.yml) to boot all dependent services for integration testing. To run integration tests there is the standard sbt **task** of **it:test**. The steps are:

- Boot Docker by either:

  - In IntelliJ (or other IDE) right-click the [docker-compose file](../docker-compose.yml) and choose **run** OR

  - In a **terminal** within root of this project:

  - ```bash
    $ docker-compose up
    ```

- Run integration tests via standard sbt task:

  - ```bash
    $ sbt it:test
    ```

However, the above approach is not recommended. Of course it is inconvenient to run the integration tests by having to execute two seperate actions - the main issue is that the dependent services are correctly running within the isolated environment of Docker but the tests themselves are not. We really should run everything within Docker to minimise the usual issues of environment settings, interference, and the Developer excuse of "It runs on my machine". (Note that unit tests should not encounter these types of issues).

It is recommended to use a **custom sbt task** which will run everything, including the tests inside Docker. Instead of the above, just execute:

```bash
$ sbt itTest
```

This task will boot all services; execute the tests; tear down all services.

For convenience, there is the sbt task **itInspect** which does the same as **itTest** but will not tear down the services, allowing inspection of the *state of play* such as looking at logs, and jumping onto containers to perform manual interactions. After using this task, the Docker containers can be brought down with:

```bash
$ docker-compose down
```

