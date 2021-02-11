package simulations

import actions._
import com.bizo.mighty.csv.CSVDictReader
import config.Config
import config.SimulationConfig._
import config.FileConfig
import io.gatling.core.Predef._
import io.gatling.http.Predef._

import scala.language.postfixOps
import scala.util.Random.shuffle

class BreadcrumbsSimulation extends Simulation {

  private val httpConf = http.baseURL(Config.navigationServiceBaseUrl)
  private val usersPerSec = users.toDouble

  def randomCategoryId = {
    val rows = CSVDictReader(Config.getResourcePath(FileConfig.categoryIdsFileName)).toList
    shuffle(rows).head.get("categoryId").get
  }

  private val categoryIdList: List[String] = List(randomCategoryId, randomCategoryId,
    randomCategoryId, randomCategoryId, randomCategoryId)

  println("Category ID list " + categoryIdList)

  private val siloActionsScenario = scenario("Breadcrumb Endpoint actions")
    .exec(GetBreadCrumbs(categoryIdList))

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
