package io.codegeet.platform.submissions

import io.codegeet.common.ExecutionStatus
import io.codegeet.common.InvocationStatus
import io.codegeet.common.Language
import io.codegeet.platform.exceptions.LanguageNotSupportedException
import io.codegeet.platform.executions.CodeExecutionResource
import io.codegeet.platform.executions.CodeExecutionService
import io.codegeet.platform.languages.languageTemplates
import io.codegeet.platform.problems.data.Problem
import org.springframework.core.io.ClassPathResource
import org.springframework.stereotype.Service
import java.nio.charset.Charset

@Service
class SubmissionExecutionService(private val executionService: CodeExecutionService) {

    fun execute(request: SubmissionExecutionRequest): SubmissionExecutionResponse {
        val executionPrefix = "[${request.executionId}]"
        return try {
            executionService.execute(
                CodeExecutionResource.CodeExecutionRequest(
                    code = buildExecutionCode(request.snippet, buildExecutionCall(request), request.language),
                    language = request.language,
                    invocations = request.invocations.map {
                        CodeExecutionResource.CodeExecutionRequest.InvocationInput(
                            //stdIn = it.stdIn,
                            arguments = listOf(request.executionId) + it.input.split("\n").orEmpty()
                        )
                    },
                ), true
            ).let { response ->
                SubmissionExecutionResponse(
                    executionId = response.executionId,
                    status = response.status,
                    time = response.totalTime,
                    error = response.error,
                    invocations = response.invocations.map { it ->
                        SubmissionExecutionResponse.Invocation(
                            input = it.arguments?.split("\n")?.drop(1)?.joinToString("\n").orEmpty(),
                            output = SubmissionExecutionResponse.InvocationOutput(
                                status = it.status,
                                stdOut = it.stdOut?.lines()
                                    ?.filterNot { line -> line.startsWith(executionPrefix) }
                                    ?.joinToString("\n")
                                    .orEmpty(),
                                output = it.stdOut?.lines()
                                    ?.firstOrNull { line -> line.startsWith(executionPrefix) }
                                    ?.removePrefix(executionPrefix)
                                    .orEmpty(),
                                stdErr = it.stdErr.orEmpty()
                            ))
                    }
                )
            }
        } catch (e: Exception) {
            SubmissionExecutionResponse(
                executionId = request.executionId,
                status = ExecutionStatus.INTERNAL_ERROR,
                error = "Failed to execute test cases. ${e.message}"
            )
        }
    }

    private fun buildExecutionCall(request: SubmissionExecutionRequest) =
        request.problem.snippets.first { it.language == request.language }.call

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

data class SubmissionExecutionRequest(
    val executionId: String,
    val snippet: String,
    val language: Language,
    val problem: Problem,
    val invocations: List<Invocation> = emptyList(),
) {
    data class Invocation(
        val input: String
    )
}

data class SubmissionExecutionResponse(
    val executionId: String,
    val status: ExecutionStatus?,
    val time: Int? = null,
    val error: String? = null,
    val invocations: List<Invocation>? = emptyList()
) {
    data class Invocation(
        val input: String,
        val output: InvocationOutput,
    )

    data class InvocationOutput(
        val status: InvocationStatus?,
        val output: String?,
        val stdOut: String?,
        val stdErr: String?
    )
}
