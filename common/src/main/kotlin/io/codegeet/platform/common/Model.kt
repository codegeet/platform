package io.codegeet.platform.common

import io.codegeet.platform.common.language.Language
import java.util.*

enum class ExecutionStatus {
    SUCCESS, COMPILATION_ERROR, INVOCATION_ERROR, INTERNAL_ERROR, TIMEOUT
}

enum class InvocationStatus {
    INTERNAL_ERROR, INVOCATION_ERROR, TIMEOUT, SUCCESS
}

data class ExecutionRequest(
    val code: String,
    val language: Language,
    val invocations: List<InvocationRequest> = listOf(InvocationRequest()),
) {
    data class InvocationRequest(
        val args: List<String>? = null,
        val stdIn: String? = null,
    )
}

data class ExecutionResult(
    val status: ExecutionStatus,
    val compilation: CompilationResult? = null,
    val invocations: List<InvocationResult> = Collections.emptyList(),
    val error: String? = null,
) {
    data class InvocationResult(
        val status: InvocationStatus,
        val details: Details? = null,
        val stdOut: String? = null,
        val stdErr: String? = null,
        val error: String? = null,
    )

    data class CompilationResult(
        val details: Details? = null,
    )

    data class Details(
        val runtime: Long? = null,
        val memory: Long? = null
    )
}

data class ExecutionJobRequest(
    val executionId: String,
    val request: ExecutionRequest
)

data class ExecutionJobReply(
    val executionId: String,
    val result: ExecutionResult
)