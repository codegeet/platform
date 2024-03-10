package io.codegeet.coderunner

class Statistics {

    companion object {
        private const val REGEX = "\\[(\\d+)\\]\\n?$"
    }

    fun buildStatisticsCall(): List<String> = getStatsCliInstalled(
        if (System.getProperty("os.name").lowercase().contains("mac")) "gtime" else "/usr/bin/time"
    )?.let {
        listOf(it, "-f", "[%M]")
    }.orEmpty()

    fun cleanStatistics(input: String): String = REGEX.toRegex()
        .find(input)?.let {
            input.removeRange(it.range)
        } ?: input

    fun getStatistics(input: String): Long? = REGEX.toRegex()
        .find(input)
        ?.groups?.get(1)
        ?.value
        ?.toLongOrNull()


    private fun getStatsCliInstalled(name: String): String? {
        return try {
            ProcessBuilder(listOf(name, "--h")).start().waitFor()
            name
        } catch (e: java.lang.Exception) {
            null
        }
    }
}
