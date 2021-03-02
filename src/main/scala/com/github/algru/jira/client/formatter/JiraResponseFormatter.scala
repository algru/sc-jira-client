package com.github.algru.jira.client.formatter

import com.github.algru.common.exception.util.ExceptionThrower
import com.github.algru.common.exception.util.ExceptionThrower.throwAndLog
import com.github.algru.common.logging.Logging
import com.github.algru.jira.client.exception.{JiraApiException, JiraWrongResponseException}
import com.github.algru.jira.client.model.{AbsenceIssue, AbsenceStatus, AbsenceType, JiraResponse}
import spray.json._
import spray.json.lenses.JsonLenses._

import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.TimeZone

object JiraResponseFormatter extends DefaultJsonProtocol with Logging {
  private val utcDateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX")

  def jiraResponseStringToAbsenceIssues(body: String): JiraResponse[AbsenceIssue] = {
    try {
      val parsedBody = body.parseJson

      val errorMessage = parsedBody.extract[Seq[String]](Symbol("errorMessages").?)
      errorMessage.foreach { e =>
        throwAndLog(s"JIRA API errors: $e", classOf[JiraApiException], log)
      }

      val warnings = parsedBody.extract[String](Symbol("warningMessages").? / *)
      warnings.foreach(w => log.warn(s"JIRA API warning: $w"))

      val startAt = parsedBody.extract[Int]("startAt")
      val maxResults = parsedBody.extract[Int]("maxResults")
      val total = parsedBody.extract[Int]("total")
      val absencesJson = parsedBody.extract[JsObject](Symbol("issues") / *)
      val absences = absencesJson.map { json =>
        val key = json.extract[String](Symbol("key"))
        val self = json.extract[String](Symbol("self"))
        val statusStr = json.extract[String](Symbol("fields") / Symbol("status") / Symbol("id"))
        val status = funnyAbsenceStatusToEnum(statusStr)
        val typeStr = json.extract[String](Symbol("fields") / Symbol("issuetype") / Symbol("id"))
        val absenceType = funnyAbsenceTypeToEnum(typeStr)
        val startStr = json.extract[String](Symbol("fields") / Symbol("customfield_11801"))
        val start = ZonedDateTime.parse(startStr, utcDateTimeFormatter).withZoneSameInstant(TimeZone.getDefault.toZoneId).toLocalDateTime
        val endStr = json.extract[String](Symbol("fields") / Symbol("customfield_11802"))
        val end = ZonedDateTime.parse(endStr, utcDateTimeFormatter).withZoneSameInstant(TimeZone.getDefault.toZoneId).toLocalDateTime
        val userName = json.extract[String](Symbol("fields") / Symbol("reporter") / Symbol("name"))
        val displayName = json.extract[String](Symbol("fields") / Symbol("reporter") / Symbol("displayName"))
        AbsenceIssue(key, self, status, absenceType, start, end, userName, displayName)
      }
      JiraResponse(startAt, maxResults, total, absences)
    } catch {
      case e: JiraApiException =>
        throw e
      case e: Exception =>
        throwAndLog(s"Cant deserialize AbsenceIssue json: ${e.getMessage}", classOf[JiraWrongResponseException], log)
    }
  }

  private def funnyAbsenceStatusToEnum(absenceStatusString: String): AbsenceStatus.Value = absenceStatusString match {
    case "10800" => AbsenceStatus.On_Agreement
    case "10801" => AbsenceStatus.Agreed
    case "6" => AbsenceStatus.Closed
  }

  private def funnyAbsenceTypeToEnum(absenceTypeId: String): AbsenceType.Value = absenceTypeId match {
    case "10800" => AbsenceType.Absence
    case "10601" => AbsenceType.SideWork
    case "11501" => AbsenceType.RemoteWork
  }
}