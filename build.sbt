name := "bvs"

version := "0.1"

scalaVersion := "2.13.5"

//https://doc.akka.io/docs/akka/current/index-classic.html
val AkkaVersion = "2.6.14"
libraryDependencies ++= Seq("com.typesafe.akka" %% "akka-actor" % AkkaVersion)
libraryDependencies += "com.h2database" % "h2" % "1.4.200"