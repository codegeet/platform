package io.codegeet.coderunner

import io.codegeet.coderunner.config.LanguageConfig
import io.codegeet.coderunner.exceptions.CompilationException
import io.codegeet.coderunner.exceptions.TimeoutException
import io.codegeet.common.ExecutionJobRequest
import io.codegeet.common.ExecutionJobResult
import io.codegeet.common.ExecutionJobStatus
import io.codegeet.common.ExecutionJobInvocationStatus
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import java.util.concurrent.TimeUnit

class Runner(private val statistics: Statistics?) {

    companion object {
        const val DEFAULT_INVOCATION_TIMEOUT_MILLIS: Long = 5_000
    }

    fun run(input: ExecutionJobRequest): ExecutionJobResult {
        return try {
            val directory = initDirectory(input)

            val compilationDetails = compileIfNeeded(input, directory)
            val invocationResult = invoke(input, directory)

            ExecutionJobResult(
                status = calculateExecutionStatus(invocationResult),
                compilation = compilationDetails,
                invocations = invocationResult,
            )
        } catch (e: CompilationException) {
            ExecutionJobResult(
                status = ExecutionJobStatus.COMPILATION_ERROR,
                error = e.message,
            )
        } catch (e: Exception) {
            ExecutionJobResult(
                status = ExecutionJobStatus.INTERNAL_ERROR,
                error = "Something went wrong during the execution: ${e.message}"
            )
        }
    }

    private fun calculateExecutionStatus(invocationResult: List<ExecutionJobResult.InvocationResult>) =
        if (invocationResult.all { it.status == ExecutionJobInvocationStatus.SUCCESS }) ExecutionJobStatus.SUCCESS else ExecutionJobStatus.INVOCATION_ERROR

    private fun compileIfNeeded(
        input: ExecutionJobRequest,
        directory: String
    ): ExecutionJobResult.CompilationDetails? =
        LanguageConfig.get(input.language).compilation?.let { command ->
            compile(command, directory)
        }

    private fun invoke(
        input: ExecutionJobRequest,
        directory: String
    ): List<ExecutionJobResult.InvocationResult> =
        (input.invocations.ifEmpty { listOf(ExecutionJobRequest.InvocationRequest()) })
            .map { invocation ->
                try {
                    val command = LanguageConfig.get(input.language).invocation
                    invocation(command, invocation, directory)
                } catch (e: TimeoutException) {
                    ExecutionJobResult.InvocationResult(
                        status = ExecutionJobInvocationStatus.TIMEOUT,
                        error = e.message
                    )
                } catch (e: Exception) {
                    ExecutionJobResult.InvocationResult(
                        status = ExecutionJobInvocationStatus.INTERNAL_ERROR,
                        error = "Something went wrong during the invocation: ${e.message}"
                    )
                }
            }

    private fun initDirectory(input: ExecutionJobRequest) = try {
        val directory = getUserHomeDirectory()
        writeFiles(input.code, directory, LanguageConfig.get(input.language).fileName)

        directory
    } catch (e: Exception) {
        throw Exception("Something went wrong during the preparation: ${e.message}", e)
    }

    private fun compile(
        compilationCommand: String,
        directory: String
    ): ExecutionJobResult.CompilationDetails? {
        try {
            val command = (statistics?.buildStatisticsCall() ?: emptyList()) + compilationCommand.split(" ")
            val processBuilder = ProcessBuilder(command)
                .directory(File(directory))

            val startTime = System.nanoTime()

            val process = processBuilder.start()
            process.waitFor()

            val stdErr = process.errorStream.readAsText()
            val stdOut = process.inputStream.readAsText()

            if (process.exitValue() != 0) {
                throw CompilationException((statistics?.cleanStatistics(stdErr) ?: stdErr).takeIf { it.isNotEmpty() } ?: stdOut)
            }

            return if (statistics != null) ExecutionJobResult.CompilationDetails(
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
        invocation: ExecutionJobRequest.InvocationRequest,
        directory: String,
    ): ExecutionJobResult.InvocationResult {
        val command = (statistics?.buildStatisticsCall() ?: emptyList()) + invocationCommand.split(" ") + invocation.arguments.orEmpty()
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

        return ExecutionJobResult.InvocationResult(
            status = if (process.exitValue() == 0) ExecutionJobInvocationStatus.SUCCESS else ExecutionJobInvocationStatus.INVOCATION_ERROR,
            details = if (statistics != null)
                ExecutionJobResult.InvocationDetails(
                    duration = (System.nanoTime() - startTime) / 1_000_000,
                    memory = statistics.getStatistics(errorStream),
                ) else null,
            stdOut = inputStream.takeIf { it.isNotEmpty() },
            stdErr = (statistics?.cleanStatistics(errorStream) ?: errorStream).takeIf { it.isNotEmpty() },
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
