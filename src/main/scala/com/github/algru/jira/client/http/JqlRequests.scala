package com.github.algru.jira.client.http

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object JqlRequests {
  def makeGetAbsenceRequest(startDate: LocalDateTime, endDate: LocalDateTime, startAt: Int, maxResults: Int): String = {
    val jql =
      "project = ABSENCE " +
        s"  AND created >= ${startDate.format(DateTimeFormatter.ISO_DATE)} " +
        s"  AND created <= ${endDate.format(DateTimeFormatter.ISO_DATE)} " +
        "ORDER BY updated DESC"

    val requestBody =
      s"""{
         |    "jql": "$jql",
         |
         |    "startAt": $startAt,
         |    "maxResults": $maxResults,
         |
         |    "fields": [
         |        "customfield_11801",
         |        "customfield_11802",
         |        "status",
         |        "issuetype",
         |        "reporter"
         |    ]
         |}""".stripMargin

    requestBody
  }
}
