package io.codegeet.platform.coderunner

import java.lang.Exception

object ProcessStats {

    private const val REGEX = "\\[(\\d+)\\]\\n?$"

    private var statsCommand: List<String> = emptyList()

    init {
        statsCommand = buildStatsCall()
    }

    fun wrapCommand(command: List<String>) = statsCommand + command

    fun <P> withTime(block: () -> P): Pair<P, Long> {
        val startTime = System.nanoTime()
        val result = block()
        val endTime = System.nanoTime()

        return result to (endTime - startTime) / 1_000_000
    }

    fun withMemory(output: String): Pair<String, Long?> =
        REGEX.toRegex().findAll(output).lastOrNull()
            ?.let { match ->
                val memory = match.groups[1]?.value?.toLongOrNull()
                val cleanedOutput = match.range.let { output.removeRange(it) }
                return Pair(cleanedOutput, memory)
            }
            ?: Pair(output, null)

    private fun buildStatsCall(): List<String> {
        val command = when {
            getOsName().contains("mac") -> "gtime"
            else -> "/usr/bin/time"
        }

        return getStatsCliInstalled(command)
            ?.let { listOf(it, "-f", "[%M]") }
            .orEmpty()
    }

    private fun getOsName() = System.getProperty("os.name").lowercase()

    private fun getStatsCliInstalled(name: String): String? {
        return try {
            ProcessBuilder(listOf(name, "--h")).start().waitFor()
            name
        } catch (e: Exception) {
            null
        }
    }
}
