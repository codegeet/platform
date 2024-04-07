package io.codegeet.platform.coderunner

import io.codegeet.platform.coderunner.config.LanguageConfig
import io.codegeet.platform.coderunner.exceptions.CompilationException
import io.codegeet.common.ExecutionJobInvocationStatus
import io.codegeet.common.ExecutionJobRequest
import io.codegeet.common.ExecutionJobResult
import io.codegeet.common.ExecutionJobStatus
import io.codegeet.platform.coderunner.exceptions.TimeLimitException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption

class Runner(private val processExecutor: ProcessExecutor) {

    companion object {
        const val DEFAULT_INVOCATION_TIMEOUT_MILLIS: Long = 5_000
    }

    fun run(input: ExecutionJobRequest): ExecutionJobResult {
        return try {
            val directory = initDirectory(input)
            val compilationDetails = compileIfNeeded(input)
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

    private fun compileIfNeeded(input: ExecutionJobRequest): ExecutionJobResult.CompilationResult? =
        LanguageConfig.get(input.language).compilation?.let { command -> compile(command) }

    private fun invoke(
        input: ExecutionJobRequest,
        directory: String
    ): List<ExecutionJobResult.InvocationResult> =
        (input.invocations.ifEmpty { listOf(ExecutionJobRequest.InvocationRequest()) })
            .map { invocation ->
                try {
                    val command = LanguageConfig.get(input.language).invocation
                    invocation(command, invocation)
                } catch (e: TimeLimitException) {
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
        compilationCommand: String
    ): ExecutionJobResult.CompilationResult {
        try {
            val process = processExecutor.execute(compilationCommand.split(" "))

            if (!process.completed) {
                throw CompilationException(process.stdErr.takeIf { it.isNotEmpty() } ?: process.stdOut)
            }

            return ExecutionJobResult.CompilationResult(
                stats = ExecutionJobResult.Stats(
                    time = process.time,
                    memory = process.memory
                )
            )
        } catch (e: CompilationException) {
            throw e
        } catch (e: Exception) {
            throw Exception("Compilation failed: ${e.message}", e)
        }
    }

    private fun invocation(
        invocationCommand: String,
        invocation: ExecutionJobRequest.InvocationRequest
    ): ExecutionJobResult.InvocationResult {

        val process = processExecutor.execute(
            command = invocationCommand.split(" ") + invocation.arguments.orEmpty(),
            input = invocation.stdIn,
            timeout = invocation.timeout ?: DEFAULT_INVOCATION_TIMEOUT_MILLIS
        )

        return ExecutionJobResult.InvocationResult(
            status = if (process.completed) ExecutionJobInvocationStatus.SUCCESS else ExecutionJobInvocationStatus.INVOCATION_ERROR,
            stats = ExecutionJobResult.Stats(
                time = process.time,
                memory = process.memory,
            ),
            stdOut = process.stdOut,
            stdErr = process.stdErr,
        )
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
