package io.codegeet.platform.execution.model

import com.fasterxml.jackson.annotation.JsonProperty
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
    val language: String,
    @JsonProperty("code")
    @Column(columnDefinition = "TEXT")
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
    val language: String,
    @JsonProperty("sync")
    val immediately: Boolean? = false,
)

data class ExecutionResponse(
    @JsonProperty("execution_id")
    val executionId: String,
    @JsonProperty("execution")
    val execution: Execution?
)
