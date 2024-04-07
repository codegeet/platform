package io.codegeet.platform.coderunner

import io.codegeet.platform.coderunner.exceptions.OutputLimitException
import io.codegeet.platform.coderunner.exceptions.TimeLimitException
import java.io.File
import java.io.InputStream
import java.util.concurrent.TimeUnit

class ProcessExecutor(private val stats: ProcessStats) {

    companion object {
        private const val DEFAULT_TIMEOUT_MILLIS: Long = 10_000
        private const val DEFAULT_OUTPUT_LIMIT_BYTES: Int = 5_000
    }

    fun execute(command: List<String>): ProcessData {
        return execute(command, null, DEFAULT_TIMEOUT_MILLIS)
    }

    fun execute(command: List<String>, input: String?, timeout: Long): ProcessData {
        val processBuilder = ProcessBuilder(stats.wrapCommand(command) ?: command)
            .directory(File(getUserHomeDirectory()))

        val (process, time) = stats.withTime {
            processBuilder.start()
                .also {
                    it.outputStream.use { it.write(input.orEmpty().toByteArray()) }
                    it.waitFor(timeout, TimeUnit.MILLISECONDS)
                }
        }

        if (process.isAlive) {
            process.destroy()
            throw TimeLimitException(timeout, TimeUnit.MILLISECONDS)
        }

        val (errorStream, memory) = process.errorStream.readAsString()
            .let { stats.withMemory(it) }

        val outputStream = process.inputStream.readAsString()

        return ProcessData(
            time = time,
            memory = memory,
            stdOut = outputStream,
            stdErr = errorStream,
            completed = process.exitValue() == 0
        )
    }

    private fun getUserHomeDirectory(): String {
        return System.getProperty("user.home")
    }

    private fun InputStream.readAsString(limit: Int = DEFAULT_OUTPUT_LIMIT_BYTES): String = this.use { inputStream ->
        String(inputStream.readNBytes(limit))
            .also {
                if (inputStream.available() > 0) throw OutputLimitException(limit)
            }
    }

    data class ProcessData(
        val time: Long,
        val memory: Long?,
        val stdOut: String,
        val stdErr: String,
        val completed: Boolean,
    )
}

