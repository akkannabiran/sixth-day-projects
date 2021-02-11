package simulations

import actions._
import com.bizo.mighty.csv.CSVDictReader
import config.Config
import config.SimulationConfig._
import io.gatling.core.Predef._
import io.gatling.http.Predef._

import scala.language.postfixOps
import scala.util.Random.shuffle

class CategoryTemplateTypeSCSimulation extends Simulation {

  private val httpConf = http.baseURL(Config.categoryTemplateServiceBaseUrl)
  private val usersPerSec = users.toDouble

  def randomCategoryIdSC = {
    val rows = CSVDictReader(Config.getResourcePath("categoryIdsSC.csv")).toList
    shuffle(rows).head.get("categoryIds").get
  }

  private val categoryActionsScenario = scenario("Category Template - SC")
    .exec { session =>
      session.set("categoryIdPayloadSC", randomCategoryIdSC)
    }
    .exec(GetCategoryTemplateDetail.apply(randomCategoryIdSC))

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