package io.codegeet.judge

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class PlatformApplication

fun main(args: Array<String>) {
	runApplication<PlatformApplication>(*args)
}
