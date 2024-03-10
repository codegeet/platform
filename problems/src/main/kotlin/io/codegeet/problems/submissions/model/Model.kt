package io.codegeet.problems.executions.model

import io.codegeet.common.Language
import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(name = "submissions")
data class Submission(
    @Id
    val submissionId: String,
    var problemId: String,
    @Enumerated(EnumType.STRING)
    val language: Language,
    @Column(columnDefinition = "TEXT")
    val snippet: String,
    @Enumerated(EnumType.STRING)
    val status: SubmissionStatus,
    val runtime: Long,
    val memory: Long,
    val createdAt: Instant,
    val executionId: String,
)

enum class SubmissionStatus {
    ACCEPTED, REJECTED, FAILED
}
