package com.github.algru.jira.client.formatter

import com.github.algru.jira.client.model.{AbsenceIssue, AbsenceStatus, AbsenceType, JiraResponse}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import java.time.LocalDateTime

class JiraResponseFormatSpec extends AnyWordSpec with Matchers {
  import TestObjects._

  "JiraResponseFormat" when {
    "deserialize String to JiraResponse" should {
      "create valid JiraResponse object" in {
        val resultJiraResponse = JiraResponseFormatter.jiraResponseStringToAbsenceIssues(validJiraResponseString)
        resultJiraResponse should be (validJiraResponse)
      }

      "throw exception if String is not valid" in {
        an [Exception] should be thrownBy {
          JiraResponseFormatter.jiraResponseStringToAbsenceIssues(invalidJiraResponseString)
        }
      }
    }
  }

  object TestObjects {
    val validJiraResponse =
      JiraResponse(
        0,
        10,
        200,
        Vector(
          AbsenceIssue(
            "RTLTEST-1",
            "http://link",
            AbsenceStatus.On_Agreement,
            AbsenceType.RemoteWork,
            LocalDateTime.of(2020, 3, 1, 9, 0, 0),
            LocalDateTime.of(2020, 3, 1, 18, 0, 0),
            LocalDateTime.of(2020, 3, 1, 22, 59, 0),
            "user",
            "User"
          )
        )
      )

    val validJiraResponseString: String =
      """{"issues":
        | [{
        |   "key":"RTLTEST-1",
        |   "self":"http://link",
        |   "fields": {
        |                "issuetype": {
        |                    "id": "11501"
        |                },
        |                "customfield_11801": "2020-03-01T09:00:00.000+0300",
        |                "reporter": {
        |                    "name": "user",
        |                    "displayName": "User"
        |                },
        |                "customfield_11802": "2020-03-01T18:00:00.000+0300",
        |                "status": {
        |                    "id": "10800"
        |                },
        |                "updated": "2020-03-01T22:59:00.000+0300"
        |            }
        |   }],
        | "maxResults":10,
        | "startAt":0,
        | "total":200
        |}""".stripMargin

    val invalidJiraResponseString: String =
      """{
        | "startAt":0,
        | "total":200
        |}""".stripMargin
  }
}
