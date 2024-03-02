package io.codegeet.sandbox.coderunner

import com.fasterxml.jackson.databind.JsonMappingException
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.codegeet.sandbox.coderunner.model.ExecutionRequest
import io.codegeet.sandbox.coderunner.model.ExecutionResult
import io.codegeet.sandbox.coderunner.model.ExecutionStatus

class Application(private val runner: Runner) {
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
            ?.let { jacksonObjectMapper().readValue(it, ExecutionRequest::class.java) }
    }

    private fun ExecutionResult.toJson(): String =
        jacksonObjectMapper().writeValueAsString(this)
}

fun main(args: Array<String>) {
    Application(Runner(Statistics())).run(args)
}
