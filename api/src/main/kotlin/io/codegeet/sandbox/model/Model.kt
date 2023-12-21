package io.codegeet.sandbox.model

import com.fasterxml.jackson.annotation.JsonProperty
import io.codegeet.sandbox.execution.Execution

data class ExecutionRequest(
    @JsonProperty("code")
    val code: String,
    @JsonProperty("language_id")
    val languageId: Language,
    @JsonProperty("sync")
    val sync: Boolean? = false,
)

data class ExecutionResponse(
    @JsonProperty("execution_id")
    val executionId: String,
    @JsonProperty("execution")
    val execution: Execution?
)
