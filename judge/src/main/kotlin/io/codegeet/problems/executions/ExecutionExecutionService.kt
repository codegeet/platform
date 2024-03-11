package io.codegeet.problems.executions

import io.codegeet.common.ExecutionJobRequest
import io.codegeet.common.ExecutionJobStatus
import io.codegeet.common.ExecutionJobInvocationStatus
import io.codegeet.common.Language
import io.codegeet.problems.exceptions.LanguageNotSupportedException
import io.codegeet.problems.job.ExecutionJobClient
import io.codegeet.problems.languages.languageTemplates
import io.codegeet.problems.problems.model.Problem
import org.springframework.core.io.ClassPathResource
import org.springframework.stereotype.Service
import java.nio.charset.Charset

@Service
class ExecutionExecutionService(    private val jobClient: ExecutionJobClient) {

    fun execute(request: ExecutionExecutionRequest, problem: Problem): ExecutionExecutionResponse {
        val executionPrefix = "[${request.executionId}]"
        return try {
            jobClient.call(
                ExecutionJobRequest(
                    code = buildExecutionCode(request.snippet, buildExecutionCall(problem, request.language), request.language),
                    language = request.language,
                    invocations = request.invocations.map {
                        ExecutionJobRequest.InvocationRequest(
                            arguments = listOf(request.executionId) + it.input.split("\n").orEmpty()
                        )
                    },
                )
            ).let { response ->
                ExecutionExecutionResponse(
                    status = response.status,
                    //time = response.totalTime,
                    error = response.error,
                    invocations = request.invocations.mapIndexed { i, input ->
                        val output = response.invocations[i]

                        ExecutionExecutionResponse.Invocation(
                            input = input.input,
                            output = ExecutionExecutionResponse.InvocationOutput(
                                status = output.status,
                                stdOut = output.stdOut?.lines()
                                    ?.filterNot { line -> line.startsWith(executionPrefix) }
                                    ?.joinToString("\n")
                                    .orEmpty(),
                                output = output.stdOut?.lines()
                                    ?.firstOrNull { line -> line.startsWith(executionPrefix) }
                                    ?.removePrefix(executionPrefix)
                                    .orEmpty(),
                                stdErr = output.stdErr.orEmpty()
                            ))
                    }
                )
            }
        } catch (e: Exception) {
            ExecutionExecutionResponse(
                status = ExecutionJobStatus.INTERNAL_ERROR,
                error = "Failed to execute test cases. ${e.message}"
            )
        }
    }

    private fun buildExecutionCall(problem: Problem, language: Language) =
        problem.snippets.first { it.language == language }.call

    private fun buildExecutionCode(snippet: String, call: String, language: Language) =
        languageTemplates[language]?.let { path ->
            readResource(path)
                .replace("{call}", call)
                .replace("{snippet}", snippet)
        } ?: throw LanguageNotSupportedException(language)

    fun readResource(path: String, charset: Charset = Charsets.UTF_8): String {
        val resource = ClassPathResource(path)
        return resource.inputStream.bufferedReader(charset).use { it.readText() }
    }
}

data class ExecutionExecutionRequest(
    val executionId: String,
    val snippet: String,
    val language: Language,
    val invocations: List<Invocation> = emptyList(),
) {
    data class Invocation(
        val input: String
    )
}

data class ExecutionExecutionResponse(
    val status: ExecutionJobStatus?,
    val time: Int? = null,
    val error: String? = null,
    val invocations: List<Invocation>? = emptyList()
) {
    data class Invocation(
        val input: String,
        val output: InvocationOutput,
    )

    data class InvocationOutput(
        val status: ExecutionJobInvocationStatus?,
        val output: String?,
        val stdOut: String?,
        val stdErr: String?
    )
}
