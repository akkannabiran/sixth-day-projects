package actions

import io.gatling.amqp.Predef._
import io.gatling.amqp.data.PublishRequest
import io.gatling.core.Predef._
import io.gatling.http.Predef._

object GetCategoryTemplateDetail {
  def apply(categoryId: String) =  {
    http("Get Category Template Details for "+categoryId)
      .get("/US/categoryTemplate/"+categoryId)
      .check(status.is(s => 200))
  }
}

