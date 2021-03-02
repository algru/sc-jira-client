package com.github.algru.jira.client.service

import akka.http.scaladsl.model.headers.BasicHttpCredentials
import com.github.algru.jira.client.formatter.JiraResponseFormatter
import com.github.algru.jira.client.http.{JqlRequests, JqlSender}
import com.github.algru.jira.client.model.{AbsenceIssue, JiraResponse}

import java.time.LocalDateTime
import scala.concurrent.Future

trait AbsenceService {
  val jiraApiUrl: String
  val jiraBasicCredentials: BasicHttpCredentials
  val jqlSender: JqlSender

  def sendGetAbsenceBatchRequest(startDate: LocalDateTime, endDate: LocalDateTime, startAt: Int, maxResults: Int): Future[JiraResponse[AbsenceIssue]] = {
    val requestBody: String = JqlRequests.makeGetAbsenceRequest(startDate, endDate, startAt, maxResults)
    jqlSender.sendJqlRequest[AbsenceIssue](
      jiraApiUrl,
      jiraBasicCredentials,
      requestBody,
      JiraResponseFormatter.jiraResponseStringToAbsenceIssues)
  }
}
