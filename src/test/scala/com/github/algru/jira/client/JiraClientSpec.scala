package com.github.algru.jira.client

import com.github.algru.jira.client.model.{AbsenceIssue, AbsenceStatus, AbsenceType, JiraResponse}
import com.github.algru.jira.client.service.AbsenceService
import org.scalamock.scalatest.MockFactory
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import java.time.LocalDateTime
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class JiraClientSpec extends AnyWordSpec with Matchers with ScalaFutures with MockFactory {
  trait MockAbsenceService extends AbsenceService {
    val mock = mockFunction[LocalDateTime, LocalDateTime, Int, Int, Future[JiraResponse[AbsenceIssue]]]
    override def sendGetAbsenceBatchRequest(startDate: LocalDateTime, endDate: LocalDateTime, startAt: Int, maxResults: Int): Future[JiraResponse[AbsenceIssue]] =
      mock(startDate, endDate, startAt, maxResults)
  }

  val testUrl = "testUrl"
  val testUsername = "test"
  val testPassword = "test"
  val testStartDate = LocalDateTime.now
  val testEndDate = LocalDateTime.now

  val jiraClient = new JiraClient(testUrl, testUsername, testPassword) with MockAbsenceService

  import TestObjects._

  "JiraClient.getAbsences" when {
    "get single absence response" should {
      "return list of one absence" in {
        jiraClient.mock.expects(*, *, 0, 1000).returning(Future.successful(validSingleAbsencesResponse))

        whenReady(jiraClient.getAbsences(testStartDate, testEndDate)) { absences =>
          absences should be (validSingleAbsencesResponse.issues)
        }
      }
    }
    "get empty absece response" should {
      "return empty list" in {
        jiraClient.mock.expects(*, *, 0, 1000).returning(Future.successful(emptyAbsencesResponse))

        whenReady(jiraClient.getAbsences(testStartDate, testEndDate)) { absences =>
          absences shouldBe empty
        }
      }
    }
    "get response with total > absences.length" should {
      "call JIRA API recursively to retrieve all absences" in {
        inSequence {
          jiraClient.mock.expects(*, *, 0, 1000).returning(Future.successful(sequenceOfAbsencesResponses(0))).noMoreThanOnce()
          jiraClient.mock.expects(*, *, 2, 2).returning(Future.successful(sequenceOfAbsencesResponses(1))).noMoreThanOnce()
          jiraClient.mock.expects(*, *, 4, 2).returning(Future.successful(sequenceOfAbsencesResponses(2))).noMoreThanOnce()
        }

        whenReady(jiraClient.getAbsences(testStartDate, testEndDate)) { absences =>
          absences should be (sequenceOfAbsencesResponses.flatMap(_.issues))
        }
      }
    }
  }

  object TestObjects {
    val validSingleAbsencesResponse =
      JiraResponse(
        1,
        1,
        1,
        Vector(
          AbsenceIssue(
            "ABSENCE-1000",
            "https://support.softclub.by/rest/api/2/issue/639515",
            AbsenceStatus.On_Agreement,
            AbsenceType.RemoteWork,
            LocalDateTime.of(2020, 3, 1, 12, 0, 0),
            LocalDateTime.of(2020, 3, 1, 21, 59, 0),
            "test",
            "Тест Тест Тест"
          )
        )
      )
    val emptyAbsencesResponse: JiraResponse[AbsenceIssue] =
      JiraResponse(
        0,
        1,
        0,
        Vector()
      )

    val sequenceOfAbsencesResponses = {
      Vector(
        JiraResponse(
          0,
          2,
          5,
          Vector(
            AbsenceIssue(
              "ABSENCE-1000",
              "https://support.softclub.by/rest/api/2/issue/639515",
              AbsenceStatus.On_Agreement,
              AbsenceType.RemoteWork,
              LocalDateTime.of(2020, 3, 1, 12, 0, 0),
              LocalDateTime.of(2020, 3, 1, 21, 59, 0),
              "test",
              "Тест Тест Тест"
            ),
            AbsenceIssue(
              "ABSENCE-1001",
              "https://support.softclub.by/rest/api/2/issue/639515",
              AbsenceStatus.On_Agreement,
              AbsenceType.RemoteWork,
              LocalDateTime.of(2020, 3, 1, 12, 0, 0),
              LocalDateTime.of(2020, 3, 1, 21, 59, 0),
              "test",
              "Тест Тест Тест"
            )
          )
        ),
        JiraResponse(
          2,
          2,
          5,
          Vector(
            AbsenceIssue(
              "ABSENCE-1002",
              "https://support.softclub.by/rest/api/2/issue/639515",
              AbsenceStatus.On_Agreement,
              AbsenceType.RemoteWork,
              LocalDateTime.of(2020, 3, 1, 12, 0, 0),
              LocalDateTime.of(2020, 3, 1, 21, 59, 0),
              "test",
              "Тест Тест Тест"
            ),
            AbsenceIssue(
              "ABSENCE-1003",
              "https://support.softclub.by/rest/api/2/issue/639515",
              AbsenceStatus.On_Agreement,
              AbsenceType.RemoteWork,
              LocalDateTime.of(2020, 3, 1, 12, 0, 0),
              LocalDateTime.of(2020, 3, 1, 21, 59, 0),
              "test",
              "Тест Тест Тест"
            )
          )
        ),
        JiraResponse(
          4,
          2,
          5,
          Vector(
            AbsenceIssue(
              "ABSENCE-1004",
              "https://support.softclub.by/rest/api/2/issue/639515",
              AbsenceStatus.On_Agreement,
              AbsenceType.RemoteWork,
              LocalDateTime.of(2020, 3, 1, 12, 0, 0),
              LocalDateTime.of(2020, 3, 1, 21, 59, 0),
              "test",
              "Тест Тест Тест"
            )
          )
        )
      )
    }
  }
}
