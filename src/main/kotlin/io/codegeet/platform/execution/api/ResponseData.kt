package io.codegeet.platform.execution.api

data class ExecutionResponse(
    val executionId: String,
    val status: ExecutionStatus,
    val time: Long?,
    val error: String?,
    val executions: List<ExecutionsOutput>?
)

data class ExecutionsOutput(
    val status: ExecutionStatus,
    val output: Output?,
)

data class Output(
    val result: String?,
    val stdOut: String?,
    val stdErr: String?
)

enum class ExecutionStatus {
    NOT_STARTED, COMPLETED, FAILED
}
