package simulations

import config.StoreIdSimulationConfig._
import config.Config
import io.gatling.core.Predef._
import io.gatling.core.structure.ScenarioBuilder
import io.gatling.http.Predef._
import requestBuilder.RequestBuilder

class GetStoresByStoreIdSimulation extends Simulation {

  private val httpConf = http.baseURL(Config.storeServiceBaseUrl)
  private val enableStubServiceToggle = Config.enableStubServiceToggle
  private val storeIdsFeeder = csv(Config.getResourcePath("storeIds.csv")).random

  val getStoresScenarioWithStoreId: ScenarioBuilder = scenario("Get stores from Store service with storeId")
    .feed(storeIdsFeeder)
    .exec(new RequestBuilder().builder().withStoreId().useStubService(enableStubServiceToggle).build())

  setUp(
    getStoresScenarioWithStoreId.inject(
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
