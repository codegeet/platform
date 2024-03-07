package io.codegeet.platform.submissions.model

import io.codegeet.common.Language
import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(name = "submissions")
data class Submission(
    @Id
    val submissionId: String,
    @Enumerated(EnumType.STRING)
    val type: SubmissionType,
    var problemId: String,
    @Enumerated(EnumType.STRING)
    val language: Language,
    @Column(columnDefinition = "TEXT")
    val snippet: String,
    val status: SubmissionStatus,

    val createdAt: Instant,

    @OneToMany(cascade = [CascadeType.ALL])
    val cases: MutableList<SubmissionCase> = mutableListOf(),

    val executionId: String? = null
)

enum class SubmissionStatus {
    NOT_STARTED, ACCEPTED, FAILED, CASES_FAILED
}

enum class SubmissionType {
    TEST, SUBMISSION
}

@Entity
@Table(name = "submission_cases")
data class SubmissionCase(
    @Id
    val caseId: String,
    val submissionId: String,
    val status: SubmissionCaseStatus,

    val input: String,
    @Column(columnDefinition = "TEXT")
    val expected: String,
    @Column(columnDefinition = "TEXT")
    val actual: String,
    @Column(columnDefinition = "TEXT")
    val stdOut: String,
    @Column(columnDefinition = "TEXT")
    val stdErr: String,
)

enum class SubmissionCaseStatus {
    PASSED, FAILED
}
