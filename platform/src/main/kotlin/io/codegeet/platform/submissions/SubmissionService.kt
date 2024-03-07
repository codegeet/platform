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
        if (request.inputs.isEmpty())
            throw IllegalArgumentException("inputs should not be empty")

        return submit(
            problemId = request.problemId,
            snippet = request.snippet,
            language = request.language,
            inputs = request.inputs,
            type = SubmissionType.TEST
        )
    }

    fun getSubmission(submissionId: String): Submission = submissionRepository.findByIdOrNull(submissionId)
        ?: throw SubmissionNotFoundException("Submission '$submissionId' not found.")

    private fun submit(
        problemId: String,
        snippet: String,
        language: Language,
        inputs: List<String> = emptyList(),
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
        processSubmission(problem, submission, inputs)
        return submission
    }

    private fun getSolution(problem: Problem) =
        problem.solution ?: throw SolutionNotFoundException(problem.problemId)

    private fun queueSubmission(
        problem: Problem,
        submission: Submission,
        inputs: List<String>,
    ) {
        processSubmission(
            problem = problem,
            submission = submission,
            inputs = inputs
        )
    }

    private fun processSubmission(
        problem: Problem,
        submission: Submission,
        inputs: List<String>,
    ) {
        val actual = execute(submission.snippet, submission.language, problem, inputs)
        //todo reason why it failed
        if (actual.status != ExecutionStatus.SUCCESS)
            submission.copy(status = FAILED)

        val expected = execute(getSolution(problem).snippet, submission.language, problem, inputs)
            .also { result ->
                if (result.status != ExecutionStatus.SUCCESS) {
                    val error = result.error
                        ?.takeIf { it.isNotEmpty() }
                        ?: result.invocations?.firstOrNull { it.status != InvocationStatus.SUCCESS }?.stdErr
                    throw InternalError("Failed to get expected results for problem: ${problem.problemId}. $error")
                }
            }

        submission.cases.addAll(
            inputs.mapIndexed { i, input ->
                val actualOutput = actual.invocations?.getOrNull(i)
                val expectedOutput = expected.invocations?.getOrNull(i)
                SubmissionCase(
                    caseId = "${submission.submissionId}_$i",
                    input = input,
                    expected = expectedOutput?.output.orEmpty(),
                    actual = actualOutput?.output.orEmpty(),
                    submissionId = submission.submissionId,
                    stdOut = actualOutput?.stdOut.orEmpty(),
                    stdErr = actualOutput?.stdErr.orEmpty(),
                    status = if (actualOutput?.output == expectedOutput?.output)
                        SubmissionCaseStatus.PASSED else SubmissionCaseStatus.FAILED
                )
            }
        )

        submission.copy(
            status = if (submission.cases.all { it.status == SubmissionCaseStatus.PASSED }) ACCEPTED else CASES_FAILED
        )
    }

    private fun execute(
        snippet: String,
        language: Language,
        problem: Problem,
        inputs: List<String>
    ) = executionService.execute(SubmissionExecutionRequest(
        executionId = UUID.randomUUID().toString(),
        snippet = snippet,
        language = language,
        problem = problem,
        invocations = inputs.map { input -> SubmissionExecutionRequest.InvocationInput(args = input.split("\n")) }
    ))
}
