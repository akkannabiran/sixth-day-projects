package simulations

import actions._
import com.bizo.mighty.csv.CSVDictReader
import config.Config
import config.SimulationConfig._
import io.gatling.core.Predef._
import io.gatling.http.Predef._

import scala.language.postfixOps
import scala.util.Random.shuffle

class CategorySimulation extends Simulation {

  private val httpConf = http.baseURL(Config.categoryServiceBaseUrl)
  private val usersPerSec = users.toDouble

  def randomCategoryId = {
    val rows = CSVDictReader(Config.getResourcePath("categoryIds.csv")).toList
    shuffle(rows).head.get("categoryAndParentCategoryIds").get
  }

  private val categoryActionsScenario = scenario("Category Details Endpoint actions")
    .exec { session =>
      session.set("categoryAndParentCategoryIdPayload", randomCategoryId)
    }
    .exec(GetCategories.apply())

  setUp(
    categoryActionsScenario.inject(
      rampUsersPerSec(1) to usersPerSec during (rampUpTime),
      constantUsersPerSec(usersPerSec) during (constantTime),
      rampUsersPerSec(usersPerSec) to 1 during (rampDownTime)
    ).protocols(httpConf)
  ).assertions(
    global.responseTime.max.lessThan(100000),
    global.failedRequests.percent.lessThan(50),
    forAll.responseTime.percentile3.lessThan(200000)
  )
}
