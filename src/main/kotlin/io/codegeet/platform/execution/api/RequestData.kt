package io.codegeet.platform.execution.api

import com.fasterxml.jackson.annotation.JsonValue
import io.codegeet.platform.config.Language

data class ExecutionRequest(
    val type: ExecutionType,
    val code: String,
    val language: Language,
    val function: Function?,
    val runs: List<RunInput> = emptyList(),
    val sync: Boolean? = false,
)

data class Function(
    val call: String,
    val input: List<FunctionInput>,
    val result: String
)

data class FunctionInput(
    val type: InputType,
    val name: String
)

data class RunInput(
    val input: Map<String, String>?,
    val stdIn: String?,
    val args:String?
)

enum class ExecutionType(private val id: String) {
    FILE("file"), FUNCTION("function");

    @JsonValue
    fun getId(): String = id
}

enum class InputType(private val id: String) {
    STRING("string"),
    INT("int"),
    INT_ARRAY("int_array");

    @JsonValue
    fun getId(): String = id
}
