package io.codegeet.sandbox.coderunner

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.codegeet.sandbox.coderunner.model.ApplicationInput
import io.codegeet.sandbox.coderunner.model.ApplicationOutput
class Application(private val runner: Runner) {
    fun run(args: Array<String>) {
        val result = runner.run(parseInput())
        println(result.toJson())
    }
    private fun parseInput(): ApplicationInput =
        jacksonObjectMapper().readValue(System.`in`.readAsText(), ApplicationInput::class.java)
    private fun ApplicationOutput.toJson(): String =
        jacksonObjectMapper().writeValueAsString(this)
}
fun main(args: Array<String>) {
    Application(Runner(Languages())).run(args)
}
