import Dependencies._

lazy val IntegrationTest = config("integration") extend Test

lazy val root = (project in file("."))
  .configs(IntegrationTest)
  .settings(
    organization := "algru",
    name := "sc-jira-client",
    version := "0.1",
    scalaVersion := "2.13.5",

    githubOwner := "algru",
    githubRepository := "sc-jira-client",
    githubTokenSource := TokenSource.GitConfig("github.token"),

    testOptions in Test := Seq(Tests.Argument("-l", "integrationTest")),

    inConfig(IntegrationTest)(Defaults.testTasks),
    testOptions in IntegrationTest := Seq(Tests.Argument("-n", "integrationTest")),
    parallelExecution in IntegrationTest := false,

    libraryDependencies ++= Seq(
      akkaHttp,
      akkaHttpSpray,
      akkaStream,
      sprayJson,
      antiDuplicationUtilities,
      scalaConfig,
      logging,
      scalaTest
    ).flatten
  )