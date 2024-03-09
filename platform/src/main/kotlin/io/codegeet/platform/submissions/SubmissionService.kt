package io.codegeet.platform.submissions

import io.codegeet.common.ExecutionStatus
import io.codegeet.common.InvocationStatus
import io.codegeet.common.Language
import io.codegeet.platform.problems.ProblemService
import io.codegeet.platform.problems.data.Problem
import io.codegeet.platform.submissions.exceptions.SolutionNotFoundException
import io.codegeet.platform.submissions.exceptions.SubmissionNotFoundException
import io.codegeet.platform.submissions.model.*
import io.codegeet.platform.submissions.model.SubmissionStatus.*
import io.codegeet.platform.submissions.resource.SubmissionResource.SubmissionRequest
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.time.Clock
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*

@Service
class SubmissionService(
    private val submissionRepository: SubmissionRepository,
    private val problemService: ProblemService,
    private val executionService: SubmissionExecutionService,
    private val clock: Clock,
) {

    fun submit(request: SubmissionRequest): Submission {
        return submit(
            problemId = request.problemId,
            snippet = request.snippet,
            language = request.language,
            type = SubmissionType.SUBMISSION
        )
    }

    fun test(request: SubmissionRequest): Submission {
        if (request.cases.isEmpty())
            throw IllegalArgumentException("inputs should not be empty")

        return submit(
            problemId = request.problemId,
            snippet = request.snippet,
            language = request.language,
            cases = request.cases,
            type = SubmissionType.TEST
        )
    }

    fun getSubmission(submissionId: String): Submission = submissionRepository.findByIdOrNull(submissionId)
        ?: throw SubmissionNotFoundException("Submission '$submissionId' not found.")

    private fun submit(
        problemId: String,
        snippet: String,
        language: Language,
        cases: List<SubmissionRequest.Case> = emptyList(),
        type: SubmissionType
    ): Submission {
        val problem = problemService.get(problemId)

        val submission = submissionRepository.save(
            Submission(
                submissionId = UUID.randomUUID().toString(),
                type = type,
                problemId = problem.problemId,
                snippet = snippet,
                language = language,
                status = NOT_STARTED,
                createdAt = Instant.now(clock).truncatedTo(ChronoUnit.MILLIS)
            )
        )
        processSubmission(problem, submission, cases)
        return submission
    }

    private fun getSolution(problem: Problem) =
        problem.solution ?: throw SolutionNotFoundException(problem.problemId)

    private fun queueSubmission(
        problem: Problem,
        submission: Submission,
        cases: List<SubmissionRequest.Case>
    ) {
        processSubmission(
            problem = problem,
            submission = submission,
            cases = cases
        )
    }

    private fun processSubmission(
        problem: Problem,
        submission: Submission,
        cases: List<SubmissionRequest.Case>
    ) {
        val actual = executionService.execute(SubmissionExecutionRequest(
            executionId = UUID.randomUUID().toString(),
            snippet = submission.snippet,
            language = submission.language,
            problem = problem,
            invocations = cases.map { SubmissionExecutionRequest.Invocation(input = it.input) }
        ))

        val processedSubmission = if (actual.status == ExecutionStatus.SUCCESS) {
            val calculatedExpected = cases.takeIf { it.any { case -> case.expected == null } }
                ?.let {
                    executionService.execute(SubmissionExecutionRequest(
                        executionId = UUID.randomUUID().toString(),
                        snippet = getSolution(problem).snippet,
                        language = submission.language,
                        problem = problem,
                        invocations = cases.filter { it.expected == null }
                            .map { SubmissionExecutionRequest.Invocation(input = it.input) }
                    ))
                        .also { result ->
                            if (result.status != ExecutionStatus.SUCCESS) {
                                val error = result.error
                                    ?.takeIf { it.isNotEmpty() }
                                    ?: result.invocations?.firstOrNull { it.output.status != InvocationStatus.SUCCESS }?.output?.stdErr
                                throw InternalError("Failed to get expected results for problem: ${problem.problemId}. $error")
                            }
                        }
                }

            submission.cases.addAll(
                cases.mapIndexed { i, case ->
                    val actualOutput = actual.invocations?.getOrNull(i)?.output
                    val expectedOutput = case.expected ?: let {
                        calculatedExpected?.invocations?.firstOrNull { it.input == case.input }?.output?.output
                    }

                    SubmissionCase(
                        caseId = "${submission.submissionId}_$i",
                        input = case.input,
                        expected = expectedOutput.orEmpty(),
                        actual = actualOutput?.output.orEmpty(),
                        submissionId = submission.submissionId,
                        stdOut = actualOutput?.stdOut.orEmpty(),
                        stdErr = actualOutput?.stdErr.orEmpty(),
                        status = if (actualOutput?.output == expectedOutput)
                            SubmissionCaseStatus.PASSED else SubmissionCaseStatus.FAILED
                    )
                }
            )

            submission.copy(
                status = if (submission.cases.all { it.status == SubmissionCaseStatus.PASSED }) ACCEPTED else CASES_FAILED
            )
        } else {
            //todo reason why it failed
            submission.copy(status = FAILED)
        }
        submissionRepository.save(processedSubmission)
    }
}
