name := """backend-exercise"""
organization := "com.example"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayJava)

scalaVersion := "2.13.3"

libraryDependencies ++= Seq(
  guice,
  ehcache,
  "org.hibernate.validator" % "hibernate-validator" % "6.1.6.Final",
  "junit" % "junit" % "4.12",
  "org.projectlombok" % "lombok" % "1.18.12",
  "org.mongodb" % "mongodb-driver-sync" % "4.1.0",
  "io.jsonwebtoken" % "jjwt" % "0.9.1",
  "de.flapdoodle.embed" % "de.flapdoodle.embed.mongo" % "2.2.0",
  "org.glassfish" % "javax.el" % "3.0.0"
)

