package io.codegeet.sandbox.coderunner

import io.codegeet.sandbox.coderunner.exceptions.CompilationException
import io.codegeet.sandbox.coderunner.exceptions.TimeoutException
import io.codegeet.sandbox.coderunner.model.ExecutionRequest
import io.codegeet.sandbox.coderunner.model.ExecutionResult
import io.codegeet.sandbox.coderunner.model.ExecutionResult.InvocationResult
import io.codegeet.sandbox.coderunner.model.ExecutionStatus
import io.codegeet.sandbox.coderunner.model.InvocationStatus
import java.io.File
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import java.util.concurrent.TimeUnit

class Runner {

    companion object {
        const val DEFAULT_INVOCATION_TIMEOUT_MILLIS: Long = 5_000
    }

    fun run(input: ExecutionRequest): ExecutionResult {
        return try {
            val directory = initDirectory(input)

            val compilationDetails = compileIfNeeded(input, directory)
            val invocationResult = invoke(input, directory)

            ExecutionResult(
                status = calculateExecutionStatus(invocationResult),
                compilation = compilationDetails,
                invocations = invocationResult,
            )
        } catch (e: CompilationException) {
            ExecutionResult(
                status = ExecutionStatus.COMPILATION_ERROR,
                error = e.message,
            )
        } catch (e: Exception) {
            ExecutionResult(
                status = ExecutionStatus.INTERNAL_ERROR,
                error = "Something went wrong during the execution: ${e.message}"
            )
        }
    }

    private fun calculateExecutionStatus(invocationResult: List<InvocationResult>) =
        if (invocationResult.all { it.status == InvocationStatus.SUCCESS }) ExecutionStatus.SUCCESS else ExecutionStatus.INVOCATION_ERROR

    private fun compileIfNeeded(input: ExecutionRequest, directory: String): ExecutionResult.CompilationDetails? =
        input.commands.compilation?.takeIfNotEmpty()?.let { command ->
            compile(command, directory)
        }

    private fun invoke(input: ExecutionRequest, directory: String): List<InvocationResult> =
        (input.invocations.ifEmpty { listOf(ExecutionRequest.InvocationDetails()) })
            .map { invocation ->
                try {
                    invocation(input.commands.invocation, invocation, directory)
                } catch (e: Exception) {
                    InvocationResult(
                        status = InvocationStatus.INTERNAL_ERROR,
                        error = "Something went wrong during the invocation: ${e.message}"
                    )
                }
            }

    private fun initDirectory(input: ExecutionRequest) = try {
        val directory = getUserHomeDirectory()
        writeFiles(input.code, directory, input.fileName)

        directory
    } catch (e: Exception) {
        throw InternalError("Something went wrong during the preparation", e)
    }

    private fun compile(command: String, directory: String): ExecutionResult.CompilationDetails {
        try {
            val processBuilder = ProcessBuilder(buildStatisticsCall() + command.split(" "))
                .directory(File(directory))

            val startTime = System.nanoTime()

            val process = processBuilder.start()

            val code = process.waitFor()
            val stdErr = process.errorStream.readAsText()
            val stdOut = process.inputStream.readAsText()

            if (code != 0) {
                throw CompilationException(cleanStatistics(stdErr) + stdOut)
            }

            return ExecutionResult.CompilationDetails(
                duration = (System.nanoTime() - startTime) / 1_000_000,
                memory = getStatistics(stdErr)
            )

        } catch (e: Exception) {
            throw Exception("Compilation failed: ${e.message}", e)
        }
    }

    private fun invocation(
        command: String,
        invocation: ExecutionRequest.InvocationDetails,
        directory: String
    ): InvocationResult {
        val command = buildStatisticsCall() + command.split(" ") + invocation.arguments
        val processBuilder = ProcessBuilder(command).directory(File(directory))

        val startTime = System.nanoTime()

        val process = processBuilder.start()
        writeStdIn(process, invocation.stdIn)

        val timeout = invocation.timeout ?: DEFAULT_INVOCATION_TIMEOUT_MILLIS
        if (!process.waitFor(timeout, TimeUnit.MILLISECONDS)) {
            throw TimeoutException("Invocation timed out after $timeout millis")
        }

        val errorStream = process.errorStream.readAsText()
        val inputStream = process.inputStream.readAsText()

        return InvocationResult(
            status = if (process.exitValue() == 0) InvocationStatus.SUCCESS else InvocationStatus.INVOCATION_ERROR,
            details = ExecutionResult.InvocationDetails(
                duration = (System.nanoTime() - startTime) / 1_000_000,
                memory = getStatistics(errorStream),
            ),
            stdOut = inputStream.takeIf { it.isNotEmpty() },
            stdErr = cleanStatistics(errorStream).takeIf { it.isNotEmpty() },
        )
    }

    private fun writeStdIn(
        process: Process,
        input: String?
    ) {
        if (input.isNullOrEmpty())
            return

        val stdIn = process.outputStream.bufferedWriter()
        input.split("\n")
            .forEach {
                stdIn.write("$it\n")
            }
        stdIn.flush()
        stdIn.close()
    }

    private fun getUserHomeDirectory(): String {
        return System.getProperty("user.home")
    }

    private fun writeFiles(
        content: String,
        directory: String,
        fileName: String
    ) {
        val path = Path.of(directory, fileName)

        Files.write(
            path,
            content.toByteArray(),
            StandardOpenOption.CREATE,
            StandardOpenOption.TRUNCATE_EXISTING
        )
    }

    private fun isCliInstalled(name: String): String? {
        return try {
            ProcessBuilder(listOf(name, "--h")).start().waitFor()
            name
        } catch (e: java.lang.Exception) {
            null
        }
    }

    private fun buildStatisticsCall(): List<String> =
        isCliInstalled(
            if (System.getProperty("os.name").lowercase().contains("mac")) "gtime" else "/usr/bin/time"
        )?.let {
            listOf(it, "-f", "[%M]")
        }.orEmpty()

    private fun cleanStatistics(input: String): String {
        val regex = "\\[(\\d+)\\]\\n?$".toRegex()

        return regex.find(input)?.let {
            input.removeRange(it.range)
        } ?: input
    }

    private fun getStatistics(input: String): Long? {
        val regex = "\\[(\\d+)\\]\\n?$".toRegex()

        return regex.find(input)
            ?.groups?.get(1)
            ?.value
            ?.toLongOrNull()
    }
}
