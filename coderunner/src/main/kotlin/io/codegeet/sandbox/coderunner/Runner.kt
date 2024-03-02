package io.codegeet.sandbox.coderunner

import io.codegeet.sandbox.coderunner.exceptions.CompilationException
import io.codegeet.sandbox.coderunner.exceptions.TimeoutException
import io.codegeet.sandbox.coderunner.model.ExecutionRequest
import io.codegeet.sandbox.coderunner.model.ExecutionResult
import io.codegeet.sandbox.coderunner.model.ExecutionResult.InvocationResult
import io.codegeet.sandbox.coderunner.model.ExecutionStatus
import io.codegeet.sandbox.coderunner.model.InvocationStatus
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import java.util.concurrent.TimeUnit

class Runner(private val stats: Statistics) {

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
        throw Exception("Something went wrong during the preparation: ${e.message}", e)
    }

    private fun compile(command: String, directory: String): ExecutionResult.CompilationDetails? {
        try {
            val processBuilder = ProcessBuilder(stats.buildStatisticsCall() + command.split(" "))
                .directory(File(directory))

            val startTime = System.nanoTime()

            val process = processBuilder.start()

            process.waitFor()
            val stdErr = process.errorStream.readAsText()
            val stdOut = process.inputStream.readAsText()

            if (process.exitValue() != 0) {
                throw CompilationException(stats.cleanStatistics(stdErr).takeIf { it.isNotEmpty() } ?: stdOut)
            }

            return if (stats.isEnabled()) ExecutionResult.CompilationDetails(
                duration = (System.nanoTime() - startTime) / 1_000_000,
                memory = stats.getStatistics(stdErr)
            ) else null

        } catch (e: CompilationException) {
            throw e
        } catch (e: Exception) {
            throw Exception("Compilation failed: ${e.message}", e)
        }
    }

    private fun invocation(
        command: String,
        invocation: ExecutionRequest.InvocationDetails,
        directory: String
    ): InvocationResult {
        val command = stats.buildStatisticsCall() + command.split(" ") + invocation.arguments
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
            details = if (stats.isEnabled())
                ExecutionResult.InvocationDetails(
                    duration = (System.nanoTime() - startTime) / 1_000_000,
                    memory = stats.getStatistics(errorStream),
                ) else null,
            stdOut = inputStream.takeIf { it.isNotEmpty() },
            stdErr = stats.cleanStatistics(errorStream).takeIf { it.isNotEmpty() },
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

}
