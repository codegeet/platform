package io.codegeet.sandbox.coderunner.model

import com.fasterxml.jackson.annotation.JsonProperty

data class ApplicationInput(
    @JsonProperty("language_id")
    val languageId: String,
    val code: String
    //todo add stdin
    //todo add command
)

data class ApplicationOutput(
    @JsonProperty("std_out")
    val stdOut: String,
    @JsonProperty("std_err")
    val stdErr: String,
    val error: String,
)
