package io.codegeet.common

import com.fasterxml.jackson.annotation.JsonValue
import java.util.Collections.emptyList

enum class Language(private val id: String) {
    CSHARP("csharp"),
    JAVA("java"),
    JS("js"),
    PYTHON("python"),
    TS("ts"),
    KOTLIN("kotlin");

    @JsonValue
    fun getId(): String = id
}

data class ContainerExecutionRequest(
    val code: String,
    val language: Language,
    val invocations: List<InvocationDetails> = listOf(InvocationDetails())
) {
    data class InvocationDetails(
        val timeout: Long? = null,
        val arguments: List<String> = emptyList(),
        val stdIn: String? = null,
    )
}

data class ContainerExecutionResult(
    val status: ExecutionStatus,
    val compilation: CompilationDetails? = null,
    val invocations: List<InvocationResult> = emptyList(),
    val error: String? = null,
) {
    data class InvocationResult(
        val status: InvocationStatus,
        val details: InvocationDetails? = null,
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