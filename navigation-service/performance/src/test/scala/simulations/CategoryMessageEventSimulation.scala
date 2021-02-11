package simulations

import actions.UpdateCategory
import com.bizo.mighty.csv.CSVDictReader
import config.SimulationConfig._
import config.FileConfig
import config.{Config, QueueConfig}

import io.gatling.amqp.Predef._
import io.gatling.amqp.config.AmqpProtocol
import io.gatling.core.Predef._
import io.gatling.http.Predef._

import scala.language.postfixOps
import scala.util.Random.shuffle

class CategoryMessageEventSimulation extends Simulation {

  private val httpConf = http.baseURL(Config.navigationServiceBaseUrl)
  private val usersPerSec = users.toDouble
  private val categoryIdFeeder = csv(Config.getResourcePath(FileConfig.categoryIdsFileName)).random

  private val siloActionsScenario = scenario("Category Message actions")

  implicit val amqpProtocol: AmqpProtocol = amqp
    .host(QueueConfig.host)
    .port(QueueConfig.port)
    .auth(QueueConfig.username, QueueConfig.password)
    .declare(queue(QueueConfig.queueName, durable = true, autoDelete = false))
    .poolSize(10)
    .build

  def randomCategoryId = {
    val rows = CSVDictReader(Config.getResourcePath("categoryIds.csv")).toList
    shuffle(rows).head.get("categoryId").get
  }

  val publishMessagesToQueue = scenario("Publish Category Update Messages To Queue")
    .feed(categoryIdFeeder)
    .exec(UpdateCategory())

  setUp(
    publishMessagesToQueue.inject(rampUsers(queueMessages) over (rampUpTime seconds)).protocols(amqpProtocol)
  ).assertions(
    global.responseTime.max.lessThan(100000),
    global.failedRequests.percent.lessThan(50),
    forAll.responseTime.percentile3.lessThan(200000)
  )
}
