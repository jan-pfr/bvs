name := "bvs"

version := "0.1"

scalaVersion := "2.13.5"

//https://doc.akka.io/docs/akka/current/index-classic.html
//https://doc.akka.io/docs/akka/2.6/cluster-usage.html
val AkkaVersion = "2.6.8"
libraryDependencies ++= Seq("com.typesafe.akka" %% "akka-actor" % AkkaVersion,
  "com.typesafe.akka" %% "akka-cluster" % "2.6.8",
  "com.typesafe.akka" %% "akka-http" % "10.2.4",
  "com.h2database" % "h2" % "1.4.200",
  "com.typesafe.akka" %% "akka-http-spray-json" % "10.2.4",
  "com.typesafe.akka" %% "akka-stream" % "2.6.8")

//https://doc.akka.io/docs/akka/2.6/cluster-usage.html