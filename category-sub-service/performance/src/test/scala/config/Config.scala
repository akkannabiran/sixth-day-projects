package config

import com.typesafe.config.ConfigFactory

object Config {
  val env = if (System.getenv("SCALA_ENV") == null) "local" else System.getenv("SCALA_ENV")
  val config = ConfigFactory.load("test-config").getConfig(env)

  val categoryTemplateServiceBaseUrl = config.getString("baseUrl")

  def getResourcePath(fileName: String): String = {
    getClass.getClassLoader.getResource(fileName).getPath
  }
}

object SimulationConfig {
  val users = Integer.getInteger("users", Config.config.getInt("simulationConfig.users")).toInt
  val rampUpTime = Integer.getInteger("rampUpTime", Config.config.getInt("simulationConfig.rampUpTime")).toInt
  val constantTime = Integer.getInteger("constantTime", Config.config.getInt("simulationConfig.constantTime")).toInt
  val rampDownTime = Integer.getInteger("rampDownTime", Config.config.getInt("simulationConfig.rampDownTime")).toInt
}
