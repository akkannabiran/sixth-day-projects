package config

import com.typesafe.config.ConfigFactory

object Config {
  val env = if (System.getenv("SCALA_ENV") == null) "local" else System.getenv("SCALA_ENV")
  val config = ConfigFactory.load("test-config").getConfig(env)

  val navigationServiceBaseUrl = config.getString("baseUrl")

  def getResourcePath(fileName: String): String = {
    getClass.getClassLoader.getResource(fileName).getPath
  }
}

object FileConfig {
    val categoryIdsFileName = Config.config.getString("fileConfig.categoryIdsFileName")
    val countryCodesFileName = Config.config.getString("fileConfig.countryCodesFileName")
}



object SimulationConfig {
  val queueMessages = Integer.getInteger("queueMessages", Config.config.getInt("simulationConfig.queueMessages")).toInt
  val users = Integer.getInteger("users", Config.config.getInt("simulationConfig.users"))
  val rampUpTime = Integer.getInteger("rampUpTime", Config.config.getInt("simulationConfig.rampUpTime"))
  val constantTime = Integer.getInteger("constantTime", Config.config.getInt("simulationConfig.constantTime"))
  val rampDownTime = Integer.getInteger("rampDownTime", Config.config.getInt("simulationConfig.rampDownTime"))
}

object QueueConfig {
  val queueName = Config.config.getString("queueConfig.queueName")
  val host = Config.config.getString("queueConfig.host")
  val port = Config.config.getInt("queueConfig.port")
  val username = Config.config.getString("queueConfig.username")
  val password = Config.config.getString("queueConfig.password")
}
