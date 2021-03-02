package com.github.algru.jira.client

import akka.actor.ActorSystem
import akka.http.scaladsl.model.headers.BasicHttpCredentials
import com.github.algru.common.logging.Logging
import com.github.algru.jira.client.http.{JqlClientHandler, JqlSender}
import com.github.algru.jira.client.model.{AbsenceIssue, JiraResponse}
import com.github.algru.jira.client.service.AbsenceService

import java.time.LocalDateTime
import scala.concurrent.{ExecutionContext, Future}

class JiraClient(url: String, username: String, password: String)
                (implicit executionContext: ExecutionContext) extends Logging { this: AbsenceService =>
  override val jiraApiUrl = s"$url/rest/api/2/search/"
  override val jiraBasicCredentials: BasicHttpCredentials = BasicHttpCredentials(username, password)
  override val jqlSender: JqlSender with JqlClientHandler = new JqlSender with JqlClientHandler {
    override implicit val actorSystem: ActorSystem = ActorSystem("jira-client")
    override implicit val executionContext: ExecutionContext = actorSystem.dispatcher
  }

  def getAbsences(startDate: LocalDateTime, endDate: LocalDateTime): Future[Seq[AbsenceIssue]] = {
    getAbsencesRecursive(startDate, endDate, 0, 1000)
  }

  private def getAbsencesRecursive(startDate: LocalDateTime, endDate: LocalDateTime, startAt: Int, maxResults: Int): Future[Seq[AbsenceIssue]] = {
    sendGetAbsenceBatchRequest(startDate, endDate, startAt, maxResults)
      .recover {
        case e: Exception =>
          log.error(s"Exception while requesting absence list: ${e.getMessage}", e)
          throw e
      }
      .flatMap {
        case JiraResponse(startAt, maxResults, total, issues) if total > startAt + issues.length =>
          getAbsencesRecursive(startDate, endDate, startAt + maxResults, maxResults).map(r =>
            JiraResponse(startAt, maxResults, total, issues ++ r)
          )
        case jiraResponse: JiraResponse[_] => Future(jiraResponse)
        case stranger => throw new IllegalArgumentException(s"Unexpected value $stranger")
      }
      .map(_.issues)
  }
}

object JiraClient {
  def apply(url: String, username: String, password: String)
           (implicit executionContext: ExecutionContext): JiraClient with AbsenceService = {
    new JiraClient(url, username, password) with AbsenceService
  }
}
