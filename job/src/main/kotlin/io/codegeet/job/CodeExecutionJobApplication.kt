package io.codegeet.job

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class CodeExecutionJobApplication

fun main(args: Array<String>) {
	runApplication<CodeExecutionJobApplication>(*args)
}
