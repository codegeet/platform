package io.codegeet.coderunner

import io.codegeet.coderunner.config.LanguageConfig
import io.codegeet.coderunner.exceptions.CompilationException
import io.codegeet.coderunner.exceptions.TimeoutException
import io.codegeet.common.ContainerExecutionRequest
import io.codegeet.common.ContainerExecutionResult
import io.codegeet.common.ExecutionStatus
import io.codegeet.common.InvocationStatus
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import java.util.concurrent.TimeUnit

class Runner(private val statistics: Statistics) {

    companion object {
        const val DEFAULT_INVOCATION_TIMEOUT_MILLIS: Long = 5_000
    }

    fun run(input: ContainerExecutionRequest): ContainerExecutionResult {
        return try {
            val directory = initDirectory(input)

            val compilationDetails = compileIfNeeded(input, directory)
            val invocationResult = invoke(input, directory)

            ContainerExecutionResult(
                status = calculateExecutionStatus(invocationResult),
                compilation = compilationDetails,
                invocations = invocationResult,
            )
        } catch (e: CompilationException) {
            ContainerExecutionResult(
                status = ExecutionStatus.COMPILATION_ERROR,
                error = e.message,
            )
        } catch (e: Exception) {
            ContainerExecutionResult(
                status = ExecutionStatus.INTERNAL_ERROR,
                error = "Something went wrong during the execution: ${e.message}"
            )
        }
    }

    private fun calculateExecutionStatus(invocationResult: List<ContainerExecutionResult.InvocationResult>) =
        if (invocationResult.all { it.status == InvocationStatus.SUCCESS }) ExecutionStatus.SUCCESS else ExecutionStatus.INVOCATION_ERROR

    private fun compileIfNeeded(
        input: ContainerExecutionRequest,
        directory: String
    ): ContainerExecutionResult.CompilationDetails? =
        LanguageConfig.get(input.language).compilation?.let { command ->
            compile(command, directory)
        }

    private fun invoke(
        input: ContainerExecutionRequest,
        directory: String
    ): List<ContainerExecutionResult.InvocationResult> =
        (input.invocations.ifEmpty { listOf(ContainerExecutionRequest.InvocationDetails()) })
            .map { invocation ->
                try {
                    val command = LanguageConfig.get(input.language).invocation
                    invocation(command, invocation, directory)
                } catch (e: TimeoutException) {
                    ContainerExecutionResult.InvocationResult(
                        status = InvocationStatus.TIMEOUT,
                        error = e.message
                    )
                } catch (e: Exception) {
                    ContainerExecutionResult.InvocationResult(
                        status = InvocationStatus.INTERNAL_ERROR,
                        error = "Something went wrong during the invocation: ${e.message}"
                    )
                }
            }

    private fun initDirectory(input: ContainerExecutionRequest) = try {
        val directory = getUserHomeDirectory()
        writeFiles(input.code, directory, LanguageConfig.get(input.language).fileName)

        directory
    } catch (e: Exception) {
        throw Exception("Something went wrong during the preparation: ${e.message}", e)
    }

    private fun compile(
        compilationCommand: String,
        directory: String,
        stats: Boolean = false
    ): ContainerExecutionResult.CompilationDetails? {
        try {
            val command = (if (stats) statistics.buildStatisticsCall() else emptyList()) + compilationCommand.split(" ")
            val processBuilder = ProcessBuilder(command)
                .directory(File(directory))

            val startTime = System.nanoTime()

            val process = processBuilder.start()
            process.waitFor()

            val stdErr = process.errorStream.readAsText()
            val stdOut = process.inputStream.readAsText()

            if (process.exitValue() != 0) {
                throw CompilationException(statistics.cleanStatistics(stdErr).takeIf { it.isNotEmpty() } ?: stdOut)
            }

            return if (stats) ContainerExecutionResult.CompilationDetails(
                duration = (System.nanoTime() - startTime) / 1_000_000,
                memory = statistics.getStatistics(stdErr)
            ) else null

        } catch (e: CompilationException) {
            throw e
        } catch (e: Exception) {
            throw Exception("Compilation failed: ${e.message}", e)
        }
    }

    private fun invocation(
        invocationCommand: String,
        invocation: ContainerExecutionRequest.InvocationDetails,
        directory: String,
        stats: Boolean = false
    ): ContainerExecutionResult.InvocationResult {
        val command =
            (if (stats) statistics.buildStatisticsCall() else emptyList()) + invocationCommand.split(" ") + invocation.arguments
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

        return ContainerExecutionResult.InvocationResult(
            status = if (process.exitValue() == 0) InvocationStatus.SUCCESS else InvocationStatus.INVOCATION_ERROR,
            details = if (stats)
                ContainerExecutionResult.InvocationDetails(
                    duration = (System.nanoTime() - startTime) / 1_000_000,
                    memory = statistics.getStatistics(errorStream),
                ) else null,
            stdOut = inputStream.takeIf { it.isNotEmpty() },
            stdErr = statistics.cleanStatistics(errorStream).takeIf { it.isNotEmpty() },
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
