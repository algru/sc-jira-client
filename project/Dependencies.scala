import sbt._

object Dependencies {
  lazy val akkaHttp = Seq(
    "com.typesafe.akka" %% "akka-http" % "10.2.4"
  )

  lazy val akkaHttpSpray = Seq(
    "com.typesafe.akka" %% "akka-http-spray-json" % "10.2.4"
  )

  lazy val akkaStream = Seq(
    "com.typesafe.akka" %% "akka-stream" % "2.6.13"
  )

  lazy val sprayJson = Seq(
    "io.spray" %%  "spray-json" % "1.3.6",
    "net.virtual-void" %%  "json-lenses" % "0.6.2"
  )

  lazy val scalaConfig = Seq(
    "com.typesafe" % "config" % "1.4.0"

  )

  lazy val logging = Seq(
    "ch.qos.logback" % "logback-classic" % "1.2.3"
  )

  lazy val scalaTest = Seq(
    "org.scalatest" %% "scalatest" % "3.2.5" % Test,
    "org.scalamock" %% "scalamock" % "5.1.0" % Test
  )
}
