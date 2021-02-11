package simulations

import config.CoordinatesSimulationConfig._
import config.Config
import io.gatling.core.Predef._
import io.gatling.core.structure.ScenarioBuilder
import io.gatling.http.Predef._
import requestBuilder.RequestBuilder

class GetStoresByCoordinatesSimulation extends Simulation {

  private val httpConf = http.baseURL(Config.storeServiceBaseUrl)
  private val enableStubServiceToggle = Config.enableStubServiceToggle
  private val radiusFeeder = csv(Config.getResourcePath("mileRadius.csv")).random
  private val coordinatesFeeder = csv(Config.getResourcePath("coordinates.csv")).random

  val getStoresScenarioWithCoordinates: ScenarioBuilder = scenario("Get Stores from Store Service using coordinates")
    .feed(coordinatesFeeder)
    .exec(new RequestBuilder().builder().withLatitudeAndLongitude().withBrandCode().useStubService(enableStubServiceToggle).build())

  val getStoresScenarioWithCoordinatesAndMileRadius: ScenarioBuilder = scenario("Get Stores from Store Service with coordinates and mile radius")
    .feed(coordinatesFeeder)
    .feed(radiusFeeder)
    .exec(new RequestBuilder().builder().withLatitudeAndLongitude().withMileRadius().withBrandCode().useStubService(enableStubServiceToggle).build())

  setUp(
    getStoresScenarioWithCoordinates.inject(
      rampUsersPerSec(1) to usersPerSec during (rampUpTime seconds),
      constantUsersPerSec(usersPerSec) during (constantTime seconds),
      rampUsersPerSec(usersPerSec) to 1 during (rampDownTime seconds)
    ).protocols(httpConf),

    getStoresScenarioWithCoordinatesAndMileRadius.inject(
      rampUsersPerSec(1) to usersPerSec during (rampUpTime seconds),
      constantUsersPerSec(usersPerSec) during (constantTime seconds),
      rampUsersPerSec(usersPerSec) to 1 during (rampDownTime seconds)
    ).protocols(httpConf)
  ).assertions(
    global.responseTime.max.lessThan(maxResponseTime),
    global.failedRequests.percent.lessThan(failedRequestsPercent),
    forAll.responseTime.percentile3.lessThan(averageResponseTime)
  )
}
