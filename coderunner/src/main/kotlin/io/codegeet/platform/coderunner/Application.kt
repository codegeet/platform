package io.codegeet.platform.coderunner

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.JsonMappingException
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.codegeet.platform.common.ExecutionRequest
import io.codegeet.platform.common.ExecutionResult
import io.codegeet.platform.common.ExecutionStatus

class Application(private val runner: Runner) {

    private val objectMapper: ObjectMapper = jacksonObjectMapper().apply {
        propertyNamingStrategy = PropertyNamingStrategies.SNAKE_CASE
        setSerializationInclusion(JsonInclude.Include.NON_NULL)
        enable(SerializationFeature.INDENT_OUTPUT)
    }

    fun run(args: Array<String>) {
        val result = try {
            parseInput()?.let { runner.run(it) }
                ?: ExecutionResult(
                    status = ExecutionStatus.INTERNAL_ERROR,
                    error = "Coderunner input is empty."
                )
        } catch (e: JsonMappingException) {
            ExecutionResult(
                status = ExecutionStatus.INTERNAL_ERROR,
                error = "Failed to parse coderunner input: ${e.message}"
            )
        }
        println(result.toJson())
    }

    private fun parseInput(): ExecutionRequest? {
        val stringBuilder = StringBuilder()
        while (true) {
            val line = readlnOrNull()
            if (line.isNullOrEmpty())
                break
            stringBuilder.append(line).append("\n")
        }

        return stringBuilder.toString()
            .takeIf { it.isNotEmpty() }
            ?.let { objectMapper.readValue(it, ExecutionRequest::class.java) }
    }

    private fun ExecutionResult.toJson(): String =
        objectMapper.writeValueAsString(this)
}

fun main(args: Array<String>) {
    Application(Runner(ProcessExecutor(stats = ProcessStats))).run(args)
}
