package io.codegeet.sandbox.execution

import com.fasterxml.jackson.annotation.JsonProperty
import io.codegeet.sandbox.model.Language
import jakarta.persistence.Entity
import jakarta.persistence.Id
import java.util.*

@Entity
data class Execution(
    @Id
    @JsonProperty("execution_id")
    val executionId: String,
    @JsonProperty("language_id")
    val languageId: Language,
    @JsonProperty("code")
    val code: String,
    @JsonProperty("std_out")
    val stdOut: String? = null,
    @JsonProperty("std_err")
    val stdErr: String? = null,
    @JsonProperty("error")
    val error: String? = null,
    @JsonProperty("exit_code")
    val exitCode: Int? = null,
)
