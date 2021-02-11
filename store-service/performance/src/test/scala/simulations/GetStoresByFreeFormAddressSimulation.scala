package simulations

import config.FreeFormAddressSimulationConfig._
import config.Config
import io.gatling.core.Predef._
import io.gatling.core.structure.ScenarioBuilder
import io.gatling.http.Predef._
import requestBuilder.RequestBuilder

class GetStoresByFreeFormAddressSimulation extends Simulation {

  private val httpConf = http.baseURL(Config.storeServiceBaseUrl)
  private val enableStubServiceToggle = Config.enableStubServiceToggle
  private val storeFeeder = csv(Config.getResourcePath("stores.csv")).random
  private val radiusFeeder = csv(Config.getResourcePath("mileRadius.csv")).random

  val getStoresScenario: ScenarioBuilder = scenario("Get Stores With freeFormAddress")
    .feed(storeFeeder)
    .exec(new RequestBuilder().builder().withFreeFromAddress().withBrandCode().withSkuId().withQuantity().useStubService(enableStubServiceToggle).build())

  val getStoresScenarioWithMileRadius: ScenarioBuilder = scenario("Get Stores from Store Service with mile radius")
    .feed(storeFeeder)
    .feed(radiusFeeder)
    .exec(new RequestBuilder().builder().withFreeFromAddress().withMileRadius().withBrandCode().withSkuId().withQuantity().useStubService(enableStubServiceToggle).build())

  setUp(
    getStoresScenario.inject(
      rampUsersPerSec(1) to usersPerSec during (rampUpTime seconds),
      constantUsersPerSec(usersPerSec) during (constantTime seconds),
      rampUsersPerSec(usersPerSec) to 1 during (rampDownTime seconds)
    ).protocols(httpConf),

    getStoresScenarioWithMileRadius.inject(
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
