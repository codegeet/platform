package io.codegeet.problems.submissions.model

import io.codegeet.common.Language
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant

@Document(collection = "submissions")
data class Submission(
    @Id
    val submissionId: String,
    var problemId: String,
    val language: Language,
    val snippet: String,
    val status: SubmissionStatus,
    val runtime: Double?,
    val memory: Double?,
    val createdAt: Instant,
    val executionId: String,
)

enum class SubmissionStatus {
    ACCEPTED, REJECTED, COMPILATION_ERROR, TIMEOUT, ERROR
}
