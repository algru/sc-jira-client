package com.github.algru.jira.client

import akka.stream.StreamTcpException
import akka.stream.scaladsl.TcpIdleTimeoutException
import com.github.algru.common.config.Configuration
import com.github.algru.jira.client.exception.JiraAuthenticationException
import com.github.algru.jira.client.model.AbsenceIssue
import com.github.algru.tag.test.IntegrationTest
import org.scalatest.concurrent.PatienceConfiguration.Timeout
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import java.time.LocalDateTime
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration.DurationInt

class JiraClientIntegrationSpec extends AnyWordSpec with Matchers with ScalaFutures {
  val testStartDate: LocalDateTime = LocalDateTime.of(2020, 12, 12, 0, 0, 0)
  val testEndDate: LocalDateTime = LocalDateTime.of(2020, 12, 15, 0, 0, 0)
  val timeout: Timeout = Timeout(11.seconds)

  "JIRA client" when {
    "setup is correct" should {
      "successfully find absence list" taggedAs IntegrationTest in {
        val jiraClient = initJiraClient
        val absencesFuture: Future[Seq[AbsenceIssue]] = jiraClient.getAbsences(testStartDate, testEndDate)
        whenReady(absencesFuture, timeout) { absences =>
          absences should not be empty
        }
      }
    }
    "credentials is incorrect" should {
      "throw JiraAuthenticationException" taggedAs IntegrationTest in {
        val jiraClient = initIncorrectCredentialsJiraClient
        val absencesFuture: Future[Seq[AbsenceIssue]] = jiraClient.getAbsences(testStartDate, testEndDate)
        whenReady(absencesFuture.failed, timeout)  { e =>
          e shouldBe an [JiraAuthenticationException]
        }
      }
    }
    "url is incorrect" should {
      "throw StreamTcpException" taggedAs IntegrationTest in {
        val jiraClient = initIncorrectUrlJiraClient
        val absencesFuture: Future[Seq[AbsenceIssue]] = jiraClient.getAbsences(testStartDate, testEndDate)
        whenReady(absencesFuture.failed, timeout) { e =>
          e shouldBe a [StreamTcpException]
        }
      }
    }
  }

  object JiraClientConfig extends Configuration {
    val url: String = config.getString("jira.url")
    val username: String = config.getString("jira.username")
    val password: String = config.getString("jira.password")
  }

  private def initJiraClient = {
    import JiraClientConfig._
    JiraClient(url, username, password)
  }

  private def initIncorrectCredentialsJiraClient = {
    import JiraClientConfig._
    JiraClient(url, "incorrectuserihope", "1")
  }

  private def initIncorrectUrlJiraClient = {
    import JiraClientConfig._
    JiraClient("https://0.0.0.1:9922", username, password)
  }
}
