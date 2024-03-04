package io.codegeet.job

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class JobApplication

fun main(args: Array<String>) {
	runApplication<JobApplication>(*args)
}
