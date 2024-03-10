package io.codegeet.problems.executions.model

import io.codegeet.common.Language
import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(name = "executions")
data class Execution(
    @Id
    val executionId: String,
    var problemId: String,
    @Enumerated(EnumType.STRING)
    val language: Language,
    @Column(columnDefinition = "TEXT")
    val snippet: String,
    @Enumerated(EnumType.STRING)
    val status: ExecutionStatus,
    val error: String? = null,
    val createdAt: Instant,

    @OneToMany(cascade = [CascadeType.ALL])
    val cases: MutableList<ExecutionCase> = mutableListOf(),
)

enum class ExecutionStatus {
    DRAFT, SUCCESS, COMPILATION_ERROR, EXECUTION_ERROR, CASE_ERROR, UNKNOWN_ERROR, TIMEOUT
}


@Entity
@Table(name = "execution_cases")
data class ExecutionCase(
    @Id
    val caseId: String,
    val executionId: String,
    val status: ExecutionCaseStatus,

    val input: String,
    @Column(columnDefinition = "TEXT")
    val expected: String,
    @Column(columnDefinition = "TEXT")
    val actual: String,
    @Column(columnDefinition = "TEXT")
    val stdOut: String?,
    @Column(columnDefinition = "TEXT")
    val stdErr: String?,
)

enum class ExecutionCaseStatus {
    PASSED, FAILED
}
