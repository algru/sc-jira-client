package com.github.algru.jira.client.http

import akka.actor.ActorSystem
import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model.HttpMethods.POST
import akka.http.scaladsl.model.StatusCodes.{OK, Unauthorized}
import akka.http.scaladsl.model.headers.HttpCredentials
import akka.http.scaladsl.model.{ContentTypes, HttpRequest, RequestEntity}
import akka.http.scaladsl.unmarshalling.Unmarshal
import com.github.algru.common.exception.util.ExceptionThrower.throwAndLog
import com.github.algru.common.logging.Logging
import com.github.algru.jira.client.exception.{JiraAuthenticationException, JiraWrongResponseException}
import com.github.algru.jira.client.model.JiraResponse

import scala.concurrent.{ExecutionContext, Future}

trait JqlSender extends Logging { this: HttpClient =>
  implicit val actorSystem: ActorSystem
  implicit val executionContext: ExecutionContext

  def sendJqlRequest[T](jiraApiUrl: String,
                        jiraBasicCredentials: HttpCredentials,
                        requestBody: String,
                        responseFormatter: String => JiraResponse[T]): Future[JiraResponse[T]] = {
    log.debug(s"JiraRequest: $requestBody")
    val batchResponseFuture = for {
      requestEntity <- Marshal(requestBody).to[RequestEntity]
      response <- sendHttpRequest(
        HttpRequest(
          method = POST,
          uri = jiraApiUrl,
          entity = requestEntity.withContentType(ContentTypes.`application/json`)
        ).addCredentials(jiraBasicCredentials)
      )
      entity <- Unmarshal(response.entity).to[String]
    } yield {
      response.status match {
        case OK =>
          log.debug(s"JiraResponse: $entity")
          responseFormatter(entity)
        case Unauthorized =>
          throwAndLog(s"Unauthorized request", classOf[JiraAuthenticationException], log)
        case status =>
          throwAndLog(s"Unexpected http status $status", classOf[JiraWrongResponseException], log)
      }

    }
    batchResponseFuture
  }
}
