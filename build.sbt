name := "http-db"

version := "1.0-SNAPSHOT"

libraryDependencies ++= Seq(
  jdbc,
  anorm,
  cache,
  "org.neo4j" % "neo4j" % "2.0.0"
)

play.Project.playScalaSettings
