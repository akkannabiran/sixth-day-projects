name := """performance"""

version := "1.0"

scalaVersion := "2.11.7"

lazy val versions = new {
  val gatling = "2.2.0-M3"
}

enablePlugins(GatlingPlugin)

resolvers ++= Seq(
  Resolver.sonatypeRepo("public")
)

libraryDependencies ++= Seq(
  "io.gatling.highcharts" % "gatling-charts-highcharts" % versions.gatling % "test",
  "io.gatling" % "gatling-test-framework" % versions.gatling % "test",
  "io.gatling" % "gatling-http" % versions.gatling % "test",
  "com.typesafe" % "config" % "1.3.1",
  "com.bizo" % "mighty-csv_2.11" % "0.2"
)
