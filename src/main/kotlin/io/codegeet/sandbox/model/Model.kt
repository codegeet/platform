package io.codegeet.sandbox.model

import com.fasterxml.jackson.annotation.JsonProperty

data class Execution(
    @JsonProperty("execution_id")
    val executionId: String,
    @JsonProperty("language_id")
    val languageId: Language,
    @JsonProperty("code")
    val code: String,
    @JsonProperty("std_out")
    val stdOut: String?,
    @JsonProperty("std_err")
    val stdErr: String?,
    @JsonProperty("error")
    val error: String?,
    @JsonProperty("exit_code")
    val exitCode: Int?,
)

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