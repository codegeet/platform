package io.codegeet.sandbox.coderunner

class Statistics(private val enabled: Boolean = true) {

    companion object {
        private const val REGEX = "\\[(\\d+)\\]\\n?$"
    }

    fun isEnabled() = enabled

    fun buildStatisticsCall(): List<String> =
        if (enabled) {
            getStatsCliInstalled(
                if (System.getProperty("os.name").lowercase().contains("mac")) "gtime" else "/usr/bin/time"
            )?.let {
                listOf(it, "-f", "[%M]")
            }.orEmpty()
        } else emptyList()

    fun cleanStatistics(input: String): String =
        if (enabled) {
            REGEX.toRegex()
                .find(input)?.let {
                    input.removeRange(it.range)
                } ?: input
        } else input

    fun getStatistics(input: String): Long? = if (enabled) {
        REGEX.toRegex()
            .find(input)
            ?.groups?.get(1)
            ?.value
            ?.toLongOrNull()
    } else null

    private fun getStatsCliInstalled(name: String): String? {
        return try {
            ProcessBuilder(listOf(name, "--h")).start().waitFor()
            name
        } catch (e: java.lang.Exception) {
            null
        }
    }
}