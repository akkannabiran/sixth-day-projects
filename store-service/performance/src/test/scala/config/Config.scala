package config

import com.typesafe.config.ConfigFactory

import scala.util.Try

object Config {
  val env = if (System.getenv("SCALA_ENV") == null) "local" else System.getenv("SCALA_ENV")
  val config = ConfigFactory.load("test-config").getConfig(env)

  val storeServiceBaseUrl = config.getString("baseUrl")
  val enableStubServiceToggle = Try(Config.config.getBoolean("enableStubServiceToggle")).getOrElse(false)
  def getResourcePath(fileName: String): String = {
    getClass.getClassLoader.getResource(fileName).getPath
  }
}

object CoordinatesSimulationConfig {
  val usersPerSec = Integer.getInteger("usersPerSec", Config.config.getInt("coordinatesSimulationConfig.usersPerSec")).toString.toDouble
  val rampUpTime = Integer.getInteger("rampUpTime", Config.config.getInt("coordinatesSimulationConfig.rampUpTime"))
  val constantTime = Integer.getInteger("constantTime", Config.config.getInt("coordinatesSimulationConfig.constantTime"))
  val rampDownTime = Integer.getInteger("rampDownTime", Config.config.getInt("coordinatesSimulationConfig.rampDownTime"))
  val maxResponseTime = Integer.getInteger("maxResponseTime", Config.config.getInt("coordinatesSimulationConfig.maxResponseTime"))
  val failedRequestsPercent = Integer.getInteger("failedRequestsPercent", Config.config.getInt("coordinatesSimulationConfig.failedRequestsPercent"))
  val averageResponseTime = Integer.getInteger("averageResponseTime", Config.config.getInt("coordinatesSimulationConfig.averageResponseTime"))
}

object FreeFormAddressSimulationConfig {
  val usersPerSec = Integer.getInteger("usersPerSec", Config.config.getInt("freeFormAddressSimulationConfig.usersPerSec")).toString.toDouble
  val rampUpTime = Integer.getInteger("rampUpTime", Config.config.getInt("freeFormAddressSimulationConfig.rampUpTime"))
  val constantTime = Integer.getInteger("constantTime", Config.config.getInt("freeFormAddressSimulationConfig.constantTime"))
  val rampDownTime = Integer.getInteger("rampDownTime", Config.config.getInt("freeFormAddressSimulationConfig.rampDownTime"))
  val maxResponseTime = Integer.getInteger("maxResponseTime", Config.config.getInt("freeFormAddressSimulationConfig.maxResponseTime"))
  val failedRequestsPercent = Integer.getInteger("failedRequestsPercent", Config.config.getInt("freeFormAddressSimulationConfig.failedRequestsPercent"))
  val averageResponseTime = Integer.getInteger("averageResponseTime", Config.config.getInt("freeFormAddressSimulationConfig.averageResponseTime"))
}

object StoreIdSimulationConfig {
  val usersPerSec = Integer.getInteger("usersPerSec", Config.config.getInt("storeIdSimulationConfig.usersPerSec")).toString.toDouble
  val rampUpTime = Integer.getInteger("rampUpTime", Config.config.getInt("storeIdSimulationConfig.rampUpTime"))
  val constantTime = Integer.getInteger("constantTime", Config.config.getInt("storeIdSimulationConfig.constantTime"))
  val rampDownTime = Integer.getInteger("rampDownTime", Config.config.getInt("storeIdSimulationConfig.rampDownTime"))
  val maxResponseTime = Integer.getInteger("maxResponseTime", Config.config.getInt("storeIdSimulationConfig.maxResponseTime"))
  val failedRequestsPercent = Integer.getInteger("failedRequestsPercent", Config.config.getInt("storeIdSimulationConfig.failedRequestsPercent"))
  val averageResponseTime = Integer.getInteger("averageResponseTime", Config.config.getInt("storeIdSimulationConfig.averageResponseTime"))
}
