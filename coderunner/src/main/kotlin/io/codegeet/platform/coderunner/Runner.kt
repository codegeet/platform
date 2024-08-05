package io.codegeet.platform.coderunner

import io.codegeet.platform.coderunner.exception.CompilationException
import io.codegeet.platform.coderunner.exception.TimeLimitException
import io.codegeet.platform.common.ExecutionRequest
import io.codegeet.platform.common.ExecutionResult
import io.codegeet.platform.common.ExecutionStatus
import io.codegeet.platform.common.InvocationStatus
import io.codegeet.platform.common.language.LanguageConfig
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption

class Runner(private val processExecutor: ProcessExecutor) {

    companion object {
        const val DEFAULT_INVOCATION_TIMEOUT_MILLIS: Long = 5_000
    }

    fun run(input: ExecutionRequest): ExecutionResult {
        return try {
            initDirectory(input)
            val compilationDetails = compileIfNeeded(input)
            val invocationResult = invoke(input)
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

    private fun calculateExecutionStatus(invocationResult: List<ExecutionResult.InvocationResult>) =
        if (invocationResult.all { it.status == InvocationStatus.SUCCESS }) ExecutionStatus.SUCCESS else ExecutionStatus.INVOCATION_ERROR

    private fun compileIfNeeded(input: ExecutionRequest): ExecutionResult.CompilationResult? =
        LanguageConfig.get(input.language).compilation?.let { command -> compile(command) }

    private fun invoke(input: ExecutionRequest): List<ExecutionResult.InvocationResult> =
        input.invocations.ifEmpty { listOf(ExecutionRequest.InvocationRequest()) }
            .map { invocation ->
                runCatching {
                    val command = LanguageConfig.get(input.language).invocation
                    invocation(command, invocation)
                }.getOrElse { e ->
                    when (e) {
                        is TimeLimitException -> ExecutionResult.InvocationResult(
                            status = InvocationStatus.TIMEOUT,
                            error = e.message
                        )

                        else -> ExecutionResult.InvocationResult(
                            status = InvocationStatus.INTERNAL_ERROR,
                            error = "Something went wrong during the invocation: ${e.message}"
                        )
                    }
                }
            }

    private fun initDirectory(input: ExecutionRequest) = try {
        val directory = getUserHomeDirectory()
        writeFiles(input.code, directory, LanguageConfig.get(input.language).fileName)

        directory
    } catch (e: Exception) {
        throw Exception("Something went wrong during the preparation: ${e.message}", e)
    }

    private fun compile(
        compilationCommand: String
    ): ExecutionResult.CompilationResult {
        try {
            val process = processExecutor.execute(compilationCommand.split(" "))

            if (!process.completed) {
                throw CompilationException(process.stdErr.takeIf { it.isNotEmpty() } ?: process.stdOut)
            }

            return ExecutionResult.CompilationResult(
                details = ExecutionResult.Details(
                    runtime = process.time,
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
        invocation: ExecutionRequest.InvocationRequest
    ): ExecutionResult.InvocationResult {

        val process = processExecutor.execute(
            command = invocationCommand.split(" ") + invocation.args.orEmpty(),
            input = invocation.stdIn,
            timeout = DEFAULT_INVOCATION_TIMEOUT_MILLIS
        )

        return ExecutionResult.InvocationResult(
            status = if (process.completed) InvocationStatus.SUCCESS else InvocationStatus.INVOCATION_ERROR,
            details = ExecutionResult.Details(
                runtime = process.time,
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
