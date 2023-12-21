package io.codegeet.sandbox.coderunner

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.codegeet.sandbox.coderunner.model.ApplicationInput
import io.codegeet.sandbox.coderunner.model.ApplicationOutput

class Application(private val runner: Runner) {
    fun run(args: Array<String>) {
        val result = parseInput()?.let { runner.run(it) }
            ?: ApplicationOutput(stdOut = "", stdErr = "", error = "Input is empty.")

        println(result.toJson())
    }

    private fun parseInput(): ApplicationInput? {
        val stringBuilder = StringBuilder()
        while (true) {
            val line = readlnOrNull()
            if (line.isNullOrEmpty())
                break
            stringBuilder.append(line).append("\n")
        }

        return stringBuilder.toString()
            .takeIf { it.isNotEmpty() }
            ?.let { jacksonObjectMapper().readValue(it, ApplicationInput::class.java) }
    }

    private fun ApplicationOutput.toJson(): String =
        jacksonObjectMapper().writeValueAsString(this)
}

fun main(args: Array<String>) {
    Application(Runner(Languages())).run(args)
}
