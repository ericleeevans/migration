// Copyright 2015-2016 Eric Evans : Scala migration
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

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


publishMavenStyle := true

publishArtifact in Test := false

publishTo <<= version { (v: String) =>
  val nexus = "https://oss.sonatype.org/"
  if (v.trim.endsWith("SNAPSHOT"))
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else
    Some("releases"  at nexus + "service/local/staging/deploy/maven2")
}

pomIncludeRepository := { _ => false }

pomExtra :=
  <url>https://github.com/ericleeevans/migration</url>
    <licenses>
      <license>
        <name>Apache 2</name>
        <url>http://www.apache.org/licenses/LICENSE-2.0.html</url>
        <distribution>repo</distribution>
      </license>
    </licenses>
    <scm>
      <url>https://github.com/ericleeevans/migration.git</url>
      <connection>scm:git:git://github.com/ericleeevans/migration.git</connection>
    </scm>
    <developers>
      <developer>
        <id>ericleeevans</id>
        <name>Eric Evans</name>
        <email>eric.lee.evans@gmail.com</email>
        <url>https://github.com/ericleeevans</url>
      </developer>
    </developers>
