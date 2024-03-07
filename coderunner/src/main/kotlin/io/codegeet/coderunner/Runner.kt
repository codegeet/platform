package io.codegeet.coderunner

import io.codegeet.coderunner.config.LanguageConfig
import io.codegeet.coderunner.exceptions.CompilationException
import io.codegeet.coderunner.exceptions.TimeoutException
import io.codegeet.common.CodeExecutionJobRequest
import io.codegeet.common.CodeExecutionJobResult
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

    fun run(input: CodeExecutionJobRequest): CodeExecutionJobResult {
        return try {
            val directory = initDirectory(input)

            val compilationDetails = compileIfNeeded(input, directory)
            val invocationResult = invoke(input, directory)

            CodeExecutionJobResult(
                status = calculateExecutionStatus(invocationResult),
                compilation = compilationDetails,
                invocations = invocationResult,
            )
        } catch (e: CompilationException) {
            CodeExecutionJobResult(
                status = ExecutionStatus.COMPILATION_ERROR,
                error = e.message,
            )
        } catch (e: Exception) {
            CodeExecutionJobResult(
                status = ExecutionStatus.INTERNAL_ERROR,
                error = "Something went wrong during the execution: ${e.message}"
            )
        }
    }

    private fun calculateExecutionStatus(invocationResult: List<CodeExecutionJobResult.InvocationResult>) =
        if (invocationResult.all { it.status == InvocationStatus.SUCCESS }) ExecutionStatus.SUCCESS else ExecutionStatus.INVOCATION_ERROR

    private fun compileIfNeeded(
        input: CodeExecutionJobRequest,
        directory: String
    ): CodeExecutionJobResult.CompilationDetails? =
        LanguageConfig.get(input.language).compilation?.let { command ->
            compile(command, directory)
        }

    private fun invoke(
        input: CodeExecutionJobRequest,
        directory: String
    ): List<CodeExecutionJobResult.InvocationResult> =
        (input.invocations.ifEmpty { listOf(CodeExecutionJobRequest.InvocationRequest()) })
            .map { invocation ->
                try {
                    val command = LanguageConfig.get(input.language).invocation
                    invocation(command, invocation, directory)
                } catch (e: TimeoutException) {
                    CodeExecutionJobResult.InvocationResult(
                        status = InvocationStatus.TIMEOUT,
                        error = e.message
                    )
                } catch (e: Exception) {
                    CodeExecutionJobResult.InvocationResult(
                        status = InvocationStatus.INTERNAL_ERROR,
                        error = "Something went wrong during the invocation: ${e.message}"
                    )
                }
            }

    private fun initDirectory(input: CodeExecutionJobRequest) = try {
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
    ): CodeExecutionJobResult.CompilationDetails? {
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

            return if (stats) CodeExecutionJobResult.CompilationDetails(
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
        invocation: CodeExecutionJobRequest.InvocationRequest,
        directory: String,
        stats: Boolean = false
    ): CodeExecutionJobResult.InvocationResult {
        val command = (if (stats) statistics.buildStatisticsCall() else emptyList()) + invocationCommand.split(" ") + invocation.arguments.orEmpty()
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

        return CodeExecutionJobResult.InvocationResult(
            status = if (process.exitValue() == 0) InvocationStatus.SUCCESS else InvocationStatus.INVOCATION_ERROR,
            details = if (stats)
                CodeExecutionJobResult.InvocationDetails(
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
