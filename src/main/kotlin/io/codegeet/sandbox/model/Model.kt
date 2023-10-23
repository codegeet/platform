package io.codegeet.sandbox.model

import com.fasterxml.jackson.annotation.JsonProperty

data class ExecutionRequest(
    @JsonProperty("code")
    val code: String,
    @JsonProperty("language_id")
    val languageId: Language
)

data class ExecutionResponse(
    @JsonProperty("execution_id")
    val executionId: String
)
