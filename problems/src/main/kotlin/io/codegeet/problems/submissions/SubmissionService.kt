package io.codegeet.problems.submissions

import io.codegeet.problems.executions.ExecutionService
import io.codegeet.problems.executions.model.ExecutionStatus
import io.codegeet.problems.executions.model.Submission
import io.codegeet.problems.executions.model.SubmissionRepository
import io.codegeet.problems.executions.model.SubmissionStatus
import io.codegeet.problems.executions.resource.ExecutionResource
import io.codegeet.problems.executions.resource.SubmissionResource
import io.codegeet.problems.problems.ProblemService
import org.springframework.stereotype.Service

@Service
class SubmissionService(
    private val repository: SubmissionRepository,
    private val problemService: ProblemService,
    private val executionService: ExecutionService,
) {

    fun submit(request: SubmissionResource.SubmissionRequest): Submission {
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

        return repository.save(
            Submission(
                submissionId = execution.executionId,
                problemId = execution.problemId,
                language = execution.language,
                snippet = execution.snippet,
                status = execution.status.toSubmissionStatus(),
                runtime = 0L,
                memory = 0L,
                createdAt = execution.createdAt,
                executionId = execution.executionId
            )
        )
    }

    fun findByProblemId(problemId: String): List<Submission> = repository.findByProblemId(problemId)

    private fun ExecutionStatus.toSubmissionStatus(): SubmissionStatus = when (this) {
        ExecutionStatus.SUCCESS -> SubmissionStatus.ACCEPTED
        ExecutionStatus.COMPILATION_ERROR,
        ExecutionStatus.EXECUTION_ERROR,
        ExecutionStatus.CASE_ERROR -> SubmissionStatus.REJECTED

        ExecutionStatus.UNKNOWN_ERROR,
        ExecutionStatus.TIMEOUT,
        ExecutionStatus.DRAFT -> SubmissionStatus.FAILED
    }
}
