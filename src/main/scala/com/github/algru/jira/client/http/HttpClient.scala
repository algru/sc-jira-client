package com.github.algru.jira.client.http

import akka.actor.ActorSystem
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}

import scala.concurrent.Future

trait HttpClient {
  def sendHttpRequest(httpRequest: HttpRequest)(implicit actorSystem: ActorSystem): Future[HttpResponse]
}
