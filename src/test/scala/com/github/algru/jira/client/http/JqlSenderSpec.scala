package com.github.algru.jira.client.http

import akka.actor.ActorSystem
import akka.http.scaladsl.model.StatusCodes.{NotFound, Unauthorized}
import akka.http.scaladsl.model.headers.BasicHttpCredentials
import akka.http.scaladsl.model.{HttpEntity, HttpRequest, HttpResponse}
import akka.util.ByteString
import com.github.algru.jira.client.exception.{JiraApiException, JiraAuthenticationException, JiraWrongResponseException}
import com.github.algru.jira.client.formatter.JiraResponseFormatter
import com.github.algru.jira.client.model.{AbsenceIssue, AbsenceStatus, AbsenceType, JiraResponse}
import org.scalamock.scalatest.MockFactory
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import java.time.LocalDateTime
import scala.concurrent.{ExecutionContext, Future}

class JqlSenderSpec extends AnyWordSpec with Matchers with ScalaFutures with MockFactory {
  trait MockJqlClientHandler extends HttpClient {
    val mock = mockFunction[HttpRequest, Future[HttpResponse]]
    override def sendHttpRequest(httpRequest: HttpRequest)(implicit actorSystem: ActorSystem): Future[HttpResponse] = mock(httpRequest)
  }

  val jqlSender = new JqlSender with MockJqlClientHandler {
    override implicit val actorSystem: ActorSystem = ActorSystem("test-jql-client")
    override implicit val executionContext: ExecutionContext = actorSystem.dispatcher
  }

  val testUrl = "testUrl"
  val testCredentials = BasicHttpCredentials("test", "test")

  import TestObjects._
  val absenceFormatter: String => JiraResponse[AbsenceIssue] = JiraResponseFormatter.jiraResponseStringToAbsenceIssues

  "JqlSender" when {
    "send valid request of class T (AbsenceIssues for example)" should {
      "return valid JiraResponse" in {
        jqlSender.mock.expects(*).returning(Future.successful(HttpResponse(entity = HttpEntity(ByteString(validResponse)))))
        val absencesFuture = jqlSender.sendJqlRequest[AbsenceIssue](testUrl, testCredentials, validRequest, absenceFormatter)

        whenReady(absencesFuture) { absences =>
          absences should be (validJiraResponse)
        }
      }
    }
    "JIRA API return errorMessages in response" should {
      "throw JiraApiException" in {
        jqlSender.mock.expects(*).returning(Future.successful(
          HttpResponse(entity = HttpEntity(ByteString(errorResponse))))
        )
        val absencesFuture = jqlSender.sendJqlRequest[AbsenceIssue](testUrl, testCredentials, validRequest, absenceFormatter)

        whenReady(absencesFuture.failed) { e =>
          e shouldBe an[JiraApiException]
        }
      }
    }
    "JIRA API return invalid json response" should {
      "throw JiraWrongResponseException" in {
        jqlSender.mock.expects(*).returning(Future.successful(
          HttpResponse(entity = HttpEntity(ByteString(invalidJsonResponse))))
        )
        val absencesFuture = jqlSender.sendJqlRequest[AbsenceIssue](testUrl, testCredentials, validRequest, absenceFormatter)

        whenReady(absencesFuture.failed) { e =>
          e shouldBe an[JiraWrongResponseException]
        }
      }
    }
    "JIRA API return http status 401 (Unauthorized)" should {
      "throw JiraAuthorizationException" in {
        jqlSender.mock.expects(*).returning(Future.successful(
          HttpResponse(status = Unauthorized, entity = HttpEntity(ByteString(validResponse))))
        )
        val absencesFuture = jqlSender.sendJqlRequest[AbsenceIssue](testUrl, testCredentials, validRequest, absenceFormatter)

        whenReady(absencesFuture.failed) { e =>
          e shouldBe an [JiraAuthenticationException]
        }
      }
    }
    "JIRA API return unexpected http status (not OK, not Unauthorized)" should {
      "throw JiraWrongResponseException" in {
        jqlSender.mock.expects(*).returning(Future.successful(
          HttpResponse(status = NotFound, entity = HttpEntity(ByteString(validResponse))))
        )
        val absencesFuture = jqlSender.sendJqlRequest[AbsenceIssue](testUrl, testCredentials, validRequest, absenceFormatter)

        whenReady(absencesFuture.failed) { e =>
          e shouldBe an[JiraWrongResponseException]
        }
      }
    }
  }

  object TestObjects {
    val validJiraResponse =
      JiraResponse(
        1,
        1,
        55598,
        Vector(
          AbsenceIssue(
            "ABSENCE-1000",
            "https://support.softclub.by/rest/api/2/issue/639515",
            AbsenceStatus.On_Agreement,
            AbsenceType.RemoteWork,
            LocalDateTime.of(2020, 3, 1, 12, 0, 0),
            LocalDateTime.of(2020, 3, 1, 21, 59, 0),
            LocalDateTime.of(2020, 3, 1, 22, 59, 0),
            "test",
            "Тест Тест Тест"
          )
        )
      )
    val validRequest =
      """{
        |    "jql": "project = ABSENCE ORDER BY updated DESC",
        |
        |    "startAt": 1,
        |    "maxResults": 1,
        |
        |    "fields": [
        |        "updated",
        |        "customfield_11801",
        |        "customfield_11802",
        |        "status",
        |        "issuetype",
        |        "reporter"
        |    ]
        |}""".stripMargin

    val validResponse =
      """{
        |    "expand": "names,schema",
        |    "startAt": 1,
        |    "maxResults": 1,
        |    "total": 55598,
        |    "issues": [
        |        {
        |            "expand": "operations,versionedRepresentations,editmeta,changelog,renderedFields",
        |            "id": "234223",
        |            "self": "https://support.softclub.by/rest/api/2/issue/639515",
        |            "key": "ABSENCE-1000",
        |            "fields": {
        |                "issuetype": {
        |                    "self": "https://support.softclub.by/rest/api/2/issuetype/11501",
        |                    "id": "11501",
        |                    "description": "Работа в удаленном режиме",
        |                    "iconUrl": "https://support.softclub.by/secure/viewavatar?size=xsmall&avatarId=10400&avatarType=issuetype",
        |                    "name": "Удаленная работа",
        |                    "subtask": false,
        |                    "avatarId": 10400
        |                },
        |                "customfield_11801": "2020-03-01T12:00:00.000+0300",
        |                "reporter": {
        |                    "self": "https://support.softclub.by/rest/api/2/user?username=test",
        |                    "name": "test",
        |                    "key": "test",
        |                    "emailAddress": "test@ttest.by",
        |                    "avatarUrls": {
        |                        "48x48": "https://www.gravatar.com/avatar/7af6e8ad594b7fbeeb9e4104fc04eb5f?d=mm&s=48",
        |                        "24x24": "https://www.gravatar.com/avatar/7af6e8ad594b7fbeeb9e4104fc04eb5f?d=mm&s=24",
        |                        "16x16": "https://www.gravatar.com/avatar/7af6e8ad594b7fbeeb9e4104fc04eb5f?d=mm&s=16",
        |                        "32x32": "https://www.gravatar.com/avatar/7af6e8ad594b7fbeeb9e4104fc04eb5f?d=mm&s=32"
        |                    },
        |                    "displayName": "Тест Тест Тест",
        |                    "active": true,
        |                    "timeZone": "Europe/Minsk"
        |                },
        |                "customfield_11802": "2020-03-01T21:59:00.000+0300",
        |                "status": {
        |                    "self": "https://support.softclub.by/rest/api/2/status/10800",
        |                    "description": "Запрос на согласовании",
        |                    "iconUrl": "https://support.softclub.by/images/icons/statuses/generic.png",
        |                    "name": "На согласовании",
        |                    "id": "10800",
        |                    "statusCategory": {
        |                        "self": "https://support.softclub.by/rest/api/2/statuscategory/4",
        |                        "id": 4,
        |                        "key": "indeterminate",
        |                        "colorName": "yellow",
        |                        "name": "В работе"
        |                    }
        |                },
        |                "updated": "2020-03-01T22:59:00.000+0300"
        |            }
        |        }
        |    ]
        |}""".stripMargin

    val invalidJsonResponse = "}"

    val errorResponse =
      """{
        | "errorMessages": ["request error"]
        |}""".stripMargin
  }
}
