package io.codegeet.platform.common

import io.codegeet.platform.common.language.Language

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
