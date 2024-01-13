package io.codegeet.sandbox.coderunner.model

import com.fasterxml.jackson.annotation.JsonProperty

data class ApplicationInput(
    @JsonProperty("code")
    val code: String,
    @JsonProperty("args")
    val args: List<String>?,
    @JsonProperty("fileName")
    val fileName: String,
    @JsonProperty("instructions")
    val instructions: Instructions
)

data class Instructions(
    @JsonProperty("build")
    val build: String? = null,
    @JsonProperty("exec")
    val exec: String,
)

data class ApplicationOutput(
    @JsonProperty("std_out")
    val stdOut: String = "",
    @JsonProperty("std_err")
    val stdErr: String = "",
    @JsonProperty("error")
    val error: String = "",
    @JsonProperty("execCode")
    val execCode: Int? = null,
    @JsonProperty("executionMillis")
    val execMillis: Long? = null,
)
