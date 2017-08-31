name := """play-scala"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.11"

libraryDependencies += jdbc
libraryDependencies += cache
libraryDependencies += ws
libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "2.0.0" % Test

val phantom = "2.9.2"
val util = "0.28.3"

libraryDependencies ++= {
  Seq(
    "com.typesafe" % "config" % "1.2.1",
    "com.outworkers" %% "phantom-dsl" % phantom,
    "com.outworkers" %% "util-testing" % util
  )
}


