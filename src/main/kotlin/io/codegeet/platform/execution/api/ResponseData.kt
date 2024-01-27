package io.codegeet.platform.execution.api

data class ExecutionResponse(
    val executionId: String,
    val runs: List<RunOutput>?
)

data class RunOutput(
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
