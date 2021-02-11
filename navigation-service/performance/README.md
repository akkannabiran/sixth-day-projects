## Navigation Service Performance Tests

This module is used to run performance tests for the navigation service

### Running the performance tests

1. To run the tests `cd <Performance test directory>` then run `sbt gatling:test`

> **NOTE:** Pass configuration environments e.g. `SCALA_ENV=devInt sbt gatling:test`
> Passing configuration parameters e.g. `SCALA_ENV=devInt sbt -DdevInt.baseUrl="<YOUR_DEV_INT_BASE_URL>" gatling:test`