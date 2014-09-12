name := "akka-slick"

version := "1.0.0-SNAPSHOT"

scalaVersion := "2.10.4"


resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"

scalacOptions ++= Seq(
	"-deprecation",
	"-target:jvm-1.7",
	"-encoding", "UTF-8",
	"-feature")

libraryDependencies <+= (scalaVersion)("org.scala-lang" % "scala-compiler" % _)

libraryDependencies ++= Seq(
	"com.typesafe.akka" %% "akka-actor" % "2.3.2",
	"com.typesafe.slick" %% "slick" % "2.1.0-M1",
	"com.typesafe" % "config" % "1.2.0",
	"org.slf4j" % "slf4j-api" % "1.7.7",
	"com.h2database" % "h2" % "1.4.177" % "test",
	"org.specs2" %% "specs2-core" % "2.3.11" % "test",
	"org.slf4j" % "slf4j-simple" % "1.7.7" % "test")