name := "migration"

organization := "org.elevans"

version := "0.1.0"

scalaVersion := "2.11.7"

crossPaths := false

fork in run := true

scalacOptions ++= Seq("-unchecked","-deprecation","-feature")

scalacOptions in (Compile,doc) ++= Seq("-author")

libraryDependencies ++= Seq(
  "com.assembla.scala-incubator" %% "graph-core" % "1.9.4",
  "commons-io" % "commons-io" % "2.4",
  "org.osgi" % "org.osgi.core" % "4.3.1",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.1.0"
)

// Test Dependencies
libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "2.2.4"
) map ( _ % "test" )


