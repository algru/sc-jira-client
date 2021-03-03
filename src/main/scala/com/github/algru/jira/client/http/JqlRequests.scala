package com.github.algru.jira.client.http

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object JqlRequests {
  private val jiraDateTimeFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")

  def makeGetAbsenceRequest(startDate: LocalDateTime, endDate: LocalDateTime, startAt: Int, maxResults: Int): String = {
    val jql =
      // endDate increased by one minute to prevent data loss because of lack of seconds
      "project = ABSENCE " +
        s"  AND updated >= '${startDate.format(jiraDateTimeFormat)}' " +
        s"  AND updated <= '${endDate.plusMinutes(1).format(jiraDateTimeFormat)}' " +
        "ORDER BY updated DESC"

    val requestBody =
      s"""{
         |    "jql": "$jql",
         |
         |    "startAt": $startAt,
         |    "maxResults": $maxResults,
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

    requestBody
  }
}
