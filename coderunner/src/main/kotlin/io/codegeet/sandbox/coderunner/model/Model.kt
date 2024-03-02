package io.codegeet.sandbox.coderunner.model

data class ExecutionRequest(
    val code: String,
    val fileName: String,
    val commands: ExecutionCommands,
    val invocations: List<InvocationDetails> = listOf(InvocationDetails()),
    val stats: Boolean? = false
) {
    data class ExecutionCommands(
        val compilation: String? = null,
        val invocation: String,
    )

    data class InvocationDetails(
        val timeout: Long? = null,
        val arguments: List<String> = emptyList(),
        val stdIn: String? = null,
    )
}

data class ExecutionResult(
    val status: ExecutionStatus,
    val compilation: CompilationDetails? = null,
    val invocations: List<InvocationResult> = emptyList(),
    val error: String? = null,
) {
    data class InvocationResult(
        val status: InvocationStatus,
        val details : InvocationDetails? = null,
        val stdOut: String? = null,
        val stdErr: String? = null,
        val error: String? = null,
    )

    data class InvocationDetails(
        val duration: Long,
        val memory: Long? = null
    )

    data class CompilationDetails(
        val duration: Long,
        val memory: Long? = null
    )
}

enum class ExecutionStatus {
    INTERNAL_ERROR, COMPILATION_ERROR, INVOCATION_ERROR, SUCCESS
}

enum class InvocationStatus {
    INTERNAL_ERROR, INVOCATION_ERROR, SUCCESS
}
