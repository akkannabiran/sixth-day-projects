package actions

import config.QueueConfig
import io.gatling.amqp.Predef._
import io.gatling.amqp.data.PublishRequest
import io.gatling.core.Predef._
import io.gatling.http.Predef._
import models.CategoryMessage

object GetSilosDesktopWithCountryCode {
  def apply() =  {
    http("Get Silos for desktop with country code")
      .get("/silos/${countryCode}/desktop")
      .check(status.is(s => 200))
  }
}

object GetSilosMobileWithCountryCode {
  def apply() =  {
    http("Get Silos for mobile with country code ${countryCode}")
      .get("/silos/${countryCode}/mobile")
        .check(status.is(s => 200))
  }
}

object GetBreadCrumbs {
  def apply(categoryIds: List[String]) =  {

    val categoriesThatWeNeedBreadcrumbsFor = categoryIds.mkString(",")

    println("Categories for breadcrumbs: " + categoriesThatWeNeedBreadcrumbsFor)

    var url = "/breadcrumbs?categoryIds="+categoriesThatWeNeedBreadcrumbsFor
    http("Get breadcrumbs for categoryIds")
      .get(url)
      .check(status.is(s => 200))
  }
}

object UpdateCategory {
  def apply() = {
    val updateCategoryMessage = new CategoryMessage(id = "${categoryId}")
    amqp("Publish UpdateCategoryMessage").publish(PublishRequest(QueueConfig.queueName, updateCategoryMessage.toJson))
  }
}
