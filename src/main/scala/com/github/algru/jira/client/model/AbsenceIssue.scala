package com.github.algru.jira.client.model

import java.time.LocalDateTime

case class AbsenceIssue(key: String,
                        self: String,
                        absenceStatus: AbsenceStatus.Value,
                        absenceType: AbsenceType.Value,
                        start: LocalDateTime,
                        end: LocalDateTime,
                        lastUpdate: LocalDateTime,
                        userName: String,
                        displayName: String)
