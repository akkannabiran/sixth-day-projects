package simulations

import java.util.concurrent.TimeUnit

import actions.GetSilosDesktopWithCountryCode
import config.Config
import config.FileConfig
import config.SimulationConfig._
import io.gatling.core.Predef._
import io.gatling.http.Predef._

import scala.concurrent.duration.FiniteDuration
import scala.language.postfixOps

class SilosDesktopSimulation extends Simulation {

  private val httpConf = http.baseURL(Config.navigationServiceBaseUrl)
  private val countryCodeFeeder = csv(Config.getResourcePath(FileConfig.countryCodesFileName)).random
  private val usersPerSec = users.toDouble
  private val pauseDuration = new FiniteDuration(5, TimeUnit.SECONDS)

  private val siloActionsScenario = scenario("Silos Desktop Endpoint actions")
    .feed(countryCodeFeeder)
    .exec(GetSilosDesktopWithCountryCode())

  setUp(
    siloActionsScenario.inject(
      rampUsersPerSec(1) to usersPerSec during(rampUpTime),
      constantUsersPerSec(usersPerSec) during(constantTime),
      rampUsersPerSec(usersPerSec) to 1 during(rampDownTime)
    ).protocols(httpConf)
  ).assertions(
    global.responseTime.max.lessThan(100000),
    global.failedRequests.percent.lessThan(50),
    forAll.responseTime.percentile3.lessThan(200000)
  )
}
