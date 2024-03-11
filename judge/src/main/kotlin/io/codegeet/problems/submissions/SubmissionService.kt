package io.codegeet.problems.submissions

import io.codegeet.problems.executions.ExecutionService
import io.codegeet.problems.executions.model.ExecutionCaseStatus
import io.codegeet.problems.executions.model.ExecutionStatus
import io.codegeet.problems.executions.resource.ExecutionResource
import io.codegeet.problems.executions.resource.SubmissionResource
import io.codegeet.problems.executions.resource.SubmissionResource.*
import io.codegeet.problems.problems.ProblemService
import io.codegeet.problems.submissions.model.Submission
import io.codegeet.problems.submissions.model.SubmissionRepository
import io.codegeet.problems.submissions.model.SubmissionStatus
import org.springframework.stereotype.Service

@Service
class SubmissionService(
    private val repository: SubmissionRepository,
    private val problemService: ProblemService,
    private val executionService: ExecutionService,
) {

    fun submit(request: SubmissionRequest): SubmissionResponse {
        val problem = problemService.get(request.problemId)

        val execution = executionService.execute(ExecutionResource.ExecutionRequest(
            problemId = request.problemId,
            snippet = request.snippet,
            language = request.language,
            cases = problem.cases.map {
                ExecutionResource.ExecutionRequest.Case(
                    input = it.input,
                    expected = it.expected
                )
            }
        ))

        val submission = Submission(
            submissionId = execution.executionId,
            problemId = execution.problemId,
            language = execution.language,
            snippet = execution.snippet,
            status = execution.status.toSubmissionStatus(),
            runtime = execution.avgRuntime,
            memory = execution.avgMemory,
            createdAt = execution.createdAt,
            executionId = execution.executionId
        )

        repository.save(submission)

        return SubmissionResponse(
            submissionId = execution.executionId,
            language = execution.language,
            snippet = execution.snippet,
            status = execution.status.toSubmissionStatus(),
            runtime = execution.avgRuntime,
            memory = execution.avgMemory,
            createdAt = execution.createdAt,
            error = execution.error,
            failedCase = execution.cases.firstOrNull { it.status != ExecutionCaseStatus.PASSED }?.let {
                SubmissionResponse.Case(
                    status = it.status,
                    input = it.input,
                    expected = it.expected,
                    actual = it.actual,
                    stdOut = it.stdOut,
                    stdErr = it.stdErr,
                )
            }
        )
    }

    fun findByProblemId(problemId: String): List<Submission> = repository.findByProblemId(problemId)

    private fun ExecutionStatus.toSubmissionStatus(): SubmissionStatus = when (this) {
        ExecutionStatus.SUCCESS -> SubmissionStatus.ACCEPTED
        ExecutionStatus.COMPILATION_ERROR -> SubmissionStatus.COMPILATION_ERROR
        ExecutionStatus.EXECUTION_ERROR,
        ExecutionStatus.CASE_ERROR -> SubmissionStatus.REJECTED
        ExecutionStatus.TIMEOUT -> SubmissionStatus.TIMEOUT
        ExecutionStatus.UNKNOWN_ERROR,
        ExecutionStatus.DRAFT -> SubmissionStatus.ERROR
    }
}
