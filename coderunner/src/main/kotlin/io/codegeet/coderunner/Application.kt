package io.codegeet.coderunner

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.JsonMappingException
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.codegeet.common.ExecutionJobRequest
import io.codegeet.common.ExecutionJobResult
import io.codegeet.common.ExecutionJobStatus

class Application(private val runner: Runner) {

    private val objectMapper: ObjectMapper = jacksonObjectMapper().apply {
        propertyNamingStrategy = PropertyNamingStrategies.SNAKE_CASE
        setSerializationInclusion(JsonInclude.Include.NON_NULL)
    }

    fun run(args: Array<String>) {
        val result = try {
            parseInput()?.let { runner.run(it) }
                ?: ExecutionJobResult(
                    status = ExecutionJobStatus.INTERNAL_ERROR,
                    error = "Coderunner input is empty."
                )
        } catch (e: JsonMappingException) {
            ExecutionJobResult(
                status = ExecutionJobStatus.INTERNAL_ERROR,
                error = "Failed to parse coderunner input: ${e.message}"
            )
        }
        println(result.toJson())
    }

    private fun parseInput(): ExecutionJobRequest? {
        val stringBuilder = StringBuilder()
        while (true) {
            val line = readlnOrNull()
            if (line.isNullOrEmpty())
                break
            stringBuilder.append(line).append("\n")
        }

        return stringBuilder.toString()
            .takeIf { it.isNotEmpty() }
            ?.let { objectMapper.readValue(it, ExecutionJobRequest::class.java) }
    }

    private fun ExecutionJobResult.toJson(): String =
        objectMapper.writeValueAsString(this)
}

fun main(args: Array<String>) {
    Application(Runner(Statistics(), TimeProvider())).run(args)
}
