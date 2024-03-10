package io.codegeet.problems.executions

import io.codegeet.common.*
import io.codegeet.problems.exceptions.LanguageNotSupportedException
import io.codegeet.problems.executions.exceptions.ExecutionNotFoundException
import io.codegeet.problems.executions.exceptions.SolutionNotFoundException
import io.codegeet.problems.executions.model.*
import io.codegeet.problems.executions.resource.ExecutionResource.ExecutionRequest
import io.codegeet.problems.job.ExecutionJobClient
import io.codegeet.problems.languages.languageTemplates
import io.codegeet.problems.problems.ProblemService
import io.codegeet.problems.problems.model.Problem
import org.springframework.core.io.ClassPathResource
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.nio.charset.Charset
import java.time.Clock
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*

@Service
class ExecutionService(
    private val executionRepository: ExecutionRepository,
    private val problemService: ProblemService,
    private val executionJobClient: ExecutionJobClient,
    private val clock: Clock,
) {

    fun execute(request: ExecutionRequest): Execution {
        val execution = Execution(
            executionId = UUID.randomUUID().toString(),
            problemId = request.problemId,
            snippet = request.snippet,
            language = request.language,
            status = ExecutionStatus.DRAFT,
            createdAt = Instant.now(clock).truncatedTo(ChronoUnit.MILLIS)
        )
        executionRepository.save(execution)

        val executed = execute(execution, request.cases)
        return executionRepository.save(executed)
    }

    fun getExecution(executionId: String): Execution = executionRepository.findByIdOrNull(executionId)
        ?: throw ExecutionNotFoundException("Execution '$executionId' not found.")

    private fun getSolution(problem: Problem) =
        problem.solution ?: throw SolutionNotFoundException(problem.problemId)

//    private fun execute(
//        execution: Execution,
//        cases: List<ExecutionRequest.Case>
//    ): Execution {
//        val problem = problemService.get(execution.problemId)
//
//        val actual = executionService.execute(ExecutionExecutionRequest(
//            executionId = UUID.randomUUID().toString(),
//            snippet = execution.snippet,
//            language = execution.language,
//            invocations = cases.map { ExecutionExecutionRequest.Invocation(input = it.input) }
//        ), problem)
//
//        val processedExecution = if (actual.status == ExecutionJobStatus.SUCCESS) {
//            val calculatedExpected = cases.takeIf { it.any { case -> case.expected == null } }
//                ?.let {
//                    executionService.execute(ExecutionExecutionRequest(
//                        executionId = UUID.randomUUID().toString(),
//                        snippet = getSolution(problem).snippet,
//                        language = execution.language,
//                        invocations = cases.filter { it.expected == null }
//                            .map { ExecutionExecutionRequest.Invocation(input = it.input) }
//                    ), problem)
//                        .also { result ->
//                            if (result.status != ExecutionJobStatus.SUCCESS) {
//                                val error = result.error
//                                    ?.takeIf { it.isNotEmpty() }
//                                    ?: result.invocations?.firstOrNull { it.output.status != ExecutionJobInvocationStatus.SUCCESS }?.output?.stdErr
//                                throw InternalError("Failed to get expected results for problem: ${problem.problemId}. $error")
//                            }
//                        }
//                }
//
//            execution.cases.addAll(
//                cases.mapIndexed { i, case ->
//                    val actualOutput = actual.invocations?.getOrNull(i)?.output
//                    val expectedOutput = case.expected ?: let {
//                        calculatedExpected?.invocations?.firstOrNull { it.input == case.input }?.output?.output
//                    }
//
//                    ExecutionCase(
//                        caseId = "${execution.executionId}_$i",
//                        input = case.input,
//                        expected = expectedOutput.orEmpty(),
//                        actual = actualOutput?.output.orEmpty(),
//                        executionId = execution.executionId,
//                        stdOut = actualOutput?.stdOut.orEmpty(),
//                        stdErr = actualOutput?.stdErr.orEmpty(),
//                        status = if (actualOutput?.output == expectedOutput)
//                            ExecutionCaseStatus.PASSED else ExecutionCaseStatus.FAILED
//                    )
//                }
//            )
//
//            execution.copy(
//                status = if (execution.cases.all { it.status == ExecutionCaseStatus.PASSED }) SUCCESS else CASE_FAILURE
//            )
//        } else {
//            //todo reason why it failed
//            execution.copy(status = actual.status)
//        }
//
//    }

    private fun execute(
        execution: Execution,
        cases: List<ExecutionRequest.Case>
    ): Execution {

        val problem = problemService.get(execution.problemId)
        val executionPrefix = "[${execution.executionId}]"

        val executionResultActual = getActualResult(problem, execution, cases)
        if (executionResultActual.status != ExecutionJobStatus.SUCCESS) {
            return execution.copy(
                status = executionResultActual.status.toExecutionStatus(),
                error = getErrorOrNull(executionResultActual)
            )
        }

        val executionResultExpected = getExpectedResultsIfNeeded(problem, execution, cases)
        if (executionResultExpected != null && executionResultExpected.status != ExecutionJobStatus.SUCCESS) {
            return execution.copy(
                status = ExecutionStatus.UNKNOWN_ERROR,
                error = "Failed to get expected results for test case. ${getErrorOrNull(executionResultExpected)}"
            )
        }

        val executedCases = cases.mapIndexed { i, case ->
            executionResultActual.invocations.getOrNull(i)
                ?.let {
                    val actual = it.stdOut?.lines()
                        ?.firstOrNull { line -> line.startsWith(executionPrefix) }
                        ?.removePrefix(executionPrefix)
                        .orEmpty()

                    val stdOut = it.stdOut?.lines()
                        ?.filterNot { line -> line.startsWith(executionPrefix) }
                        ?.joinToString("\n")

                    val stdErr = it.stdErr

                    val expected = case.expected
                        ?: let {
                            executionResultExpected?.invocations?.getOrNull(i)?.stdOut?.lines()
                                ?.firstOrNull { line -> line.startsWith(executionPrefix) }
                                ?.removePrefix(executionPrefix)
                                .orEmpty()
                        }

                    ExecutionCase(
                        caseId = "${execution.executionId}_$i",
                        executionId = execution.executionId,
                        status = if (actual == expected) ExecutionCaseStatus.PASSED else ExecutionCaseStatus.FAILED,
                        input = case.input,
                        expected = expected,
                        actual = actual,
                        stdOut = stdOut,
                        stdErr = stdErr?.takeIf { it.isNotEmpty() } ?: it.error,
                    )
                }


        }
        execution.cases.addAll(executedCases.filterNotNull())

        if (executedCases.any { it == null }) {
            return execution.copy(
                status = ExecutionStatus.UNKNOWN_ERROR,
                error = "Failed to get actual result for one of the test cases."
            )
        }

        return execution.copy(
            status = if (execution.cases.all { it.status == ExecutionCaseStatus.PASSED }) ExecutionStatus.SUCCESS else ExecutionStatus.CASE_ERROR
        )
    }

    private fun getExpectedResultsIfNeeded(
        problem: Problem,
        execution: Execution,
        cases: List<ExecutionRequest.Case>,
    ): ExecutionJobResult? {
        if (cases.all { case -> case.expected != null })
            return null

        val solution = getSolution(problem)
        return executionJobClient.call(
            ExecutionJobRequest(
                code = buildExecutionCode(
                    solution.snippet,
                    buildExecutionCall(problem, solution.language),
                    solution.language
                ),
                language = solution.language,
                invocations = cases
                    .map {
                        ExecutionJobRequest.InvocationRequest(
                            arguments = listOf(execution.executionId) + it.input.split("\n")
                        )
                    }
            ))
    }

    private fun getActualResult(
        problem: Problem,
        execution: Execution,
        cases: List<ExecutionRequest.Case>,
    ) = executionJobClient.call(
        ExecutionJobRequest(
            code = buildExecutionCode(
                execution.snippet,
                buildExecutionCall(problem, execution.language),
                execution.language
            ),
            language = execution.language,
            invocations = cases.map {
                ExecutionJobRequest.InvocationRequest(
                    arguments = listOf(execution.executionId) + it.input.split("\n").orEmpty()
                )
            },
        )
    )

    private fun getErrorOrNull(actual: ExecutionJobResult) = (actual.error?.takeIf { it.isNotEmpty() }
        ?: actual.invocations
            .firstOrNull { it.status != ExecutionJobInvocationStatus.SUCCESS }
            ?.let { invocation -> (invocation.stdErr?.takeIf { it.isNotEmpty() }) ?: invocation.error })

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

    private fun ExecutionJobStatus.toExecutionStatus(): ExecutionStatus = when (this) {
        ExecutionJobStatus.SUCCESS -> ExecutionStatus.SUCCESS
        ExecutionJobStatus.COMPILATION_ERROR -> ExecutionStatus.COMPILATION_ERROR
        ExecutionJobStatus.INVOCATION_ERROR -> ExecutionStatus.EXECUTION_ERROR
        ExecutionJobStatus.INTERNAL_ERROR -> ExecutionStatus.UNKNOWN_ERROR
        ExecutionJobStatus.TIMEOUT -> ExecutionStatus.TIMEOUT
    }
}
