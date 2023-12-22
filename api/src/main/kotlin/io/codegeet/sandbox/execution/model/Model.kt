package io.codegeet.sandbox.execution.model

import com.fasterxml.jackson.annotation.JsonProperty
import io.codegeet.sandbox.languages.Language
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import java.time.Instant

@Entity
data class Execution(
    @Id
    @JsonProperty("execution_id")
    val executionId: String,
    @JsonProperty("language")
    val language: Language,
    @JsonProperty("code")
    val code: String,
    @JsonProperty("std_out")
    @Column(columnDefinition = "TEXT")
    val stdOut: String? = null,
    @JsonProperty("std_err")
    @Column(columnDefinition = "TEXT")
    val stdErr: String? = null,
    @JsonProperty("error")
    @Column(columnDefinition = "TEXT")
    val error: String? = null,
    @JsonProperty("exit_code")
    val exitCode: Int? = null,
    @JsonProperty("created_at")
    val createdAt: Instant,
    @JsonProperty("executed_at")
    val executedAt: Instant? = null,
    @JsonProperty("total_execution_millis")
    val totalExecutionMillis: Long? = null,
)

data class ExecutionRequest(
    @JsonProperty("code")
    val code: String,
    @JsonProperty("language")
    val language: Language,
    @JsonProperty("sync")
    val sync: Boolean? = false,
)

data class ExecutionResponse(
    @JsonProperty("execution_id")
    val executionId: String,
    @JsonProperty("execution")
    val execution: Execution?
)
