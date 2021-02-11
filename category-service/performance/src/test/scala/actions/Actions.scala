package actions

import config.QueueConfig
import io.gatling.amqp.Predef._
import io.gatling.amqp.data.PublishRequest
import io.gatling.core.Predef._
import io.gatling.http.Predef._

object GetCategories {
  def apply() = {
    http("Get Categories")
      .post("/" + "/US/categories")
      .header("Content-Type", "application/json")
      .body(StringBody("""${categoryAndParentCategoryIdPayload}"""))
      .check(status.is(s => 200))
  }
}

