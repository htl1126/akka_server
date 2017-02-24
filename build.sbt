name := "Datalogue Coding Challenge"

version := "1.0"

scalaVersion := "2.11.8"

lazy val akkaHttpVersion = "2.4.17"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-http-core" % "10.0.3",
  "com.typesafe.akka" %% "akka-http" % "10.0.3",
  "com.typesafe.akka" %% "akka-http-testkit" % "10.0.3",
  "com.typesafe.akka" %% "akka-http-spray-json" % "10.0.3",
  "com.typesafe.akka" %% "akka-http-jackson" % "10.0.3",
  "com.typesafe.akka" %% "akka-http-xml" % "10.0.3"
)

// https://mvnrepository.com/artifact/uk.gov.nationalarchives/csv-validator-java-api
// Dependency resolver http://qiita.com/nyango/items/ecd47bb6902716d08b90
//resolvers += "Gilt Group Bintray Repo" at "http://dl.bintray.com/giltgroupe/maven"
//libraryDependencies += "uk.gov.nationalarchives" % "csv-validator-java-api" % "1.1.5"

// https://mvnrepository.com/artifact/io.circe/circe-core_2.11
libraryDependencies += "io.circe" % "circe-core_2.11" % "0.7.0"
