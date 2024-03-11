package io.codegeet.problems.executions.model

import io.codegeet.common.Language
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant

@Document(collection = "executions")
data class Execution(
    @Id
    val executionId: String,
    var problemId: String,
    val language: Language,
    val snippet: String,
    val status: ExecutionStatus,
    val error: String? = null,
    val createdAt: Instant,
    val avgRuntime: Double? = null,
    val avgMemory: Double? = null,
    val cases: MutableList<ExecutionCase> = mutableListOf(),
)

data class ExecutionCase(
    val status: ExecutionCaseStatus,
    val input: String,
    val expected: String,
    val actual: String,
    val stdOut: String?,
    val stdErr: String?,
    val runtime: Long?,
    val memory: Long?,
)

enum class ExecutionCaseStatus {
    PASSED, FAILED
}

enum class ExecutionStatus {
    DRAFT, SUCCESS, COMPILATION_ERROR, EXECUTION_ERROR, CASE_ERROR, UNKNOWN_ERROR, TIMEOUT
}
