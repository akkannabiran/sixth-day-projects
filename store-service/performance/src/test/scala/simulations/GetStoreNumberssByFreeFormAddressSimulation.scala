package simulations

import config.FreeFormAddressSimulationConfig._
import config.Config
import io.gatling.core.Predef._
import io.gatling.core.structure.ScenarioBuilder
import io.gatling.http.Predef._
import requestBuilder.RequestBuilder

class GetStoreNumbersByFreeFormAddressSimulation extends Simulation {

  private val httpConf = http.baseURL(Config.storeServiceBaseUrl)
  private val enableStubServiceToggle = Config.enableStubServiceToggle
  private val storeFeeder = csv(Config.getResourcePath("stores.csv")).random
  private val radiusFeeder = csv(Config.getResourcePath("mileRadius.csv")).random

  val getStoreNumbersScenario: ScenarioBuilder = scenario("Get StoreNumbers With FreeFormAddress")
    .feed(storeFeeder)
    .feed(radiusFeeder)
    .exec(new RequestBuilder().builder().withFreeFromAddress().withMileRadius().withBrandCode().useStubService(enableStubServiceToggle).overrideRequestName("Get StoreNumbers").build())

  setUp(
    getStoreNumbersScenario.inject(
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
