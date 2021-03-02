package com.github.algru.jira.client.model

case class JiraResponse[T](startAt: Int, maxResults: Int, total: Int, issues: Seq[T])

