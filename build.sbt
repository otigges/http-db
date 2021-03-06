name := "http-db"

version := "1.0-SNAPSHOT"

libraryDependencies ++= Seq(
  jdbc,
  anorm,
  cache,
  "org.neo4j" % "neo4j" % "2.0.0",
  "org.specs2" %% "specs2" % "2.3.10" % "test",
  "com.github.nscala-time" %% "nscala-time" % "1.2.0"
)



play.Project.playScalaSettings
