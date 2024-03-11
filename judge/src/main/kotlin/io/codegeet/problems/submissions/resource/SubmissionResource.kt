package io.codegeet.problems.executions.resource

import io.codegeet.common.Language
import io.codegeet.problems.executions.model.ExecutionCaseStatus
import io.codegeet.problems.submissions.SubmissionService
import io.codegeet.problems.submissions.model.Submission
import io.codegeet.problems.submissions.model.SubmissionStatus
import org.springframework.web.bind.annotation.*
import java.time.Instant

@RestController
@RequestMapping("api/submissions")
class SubmissionResource(
    private val submissionService: SubmissionService
) {

    @PostMapping
    fun submit(@RequestBody request: SubmissionRequest): SubmissionResponse {
        return submissionService.submit(request)
    }

    @GetMapping
    fun getByProblemId(@RequestParam problemId: String): List<Submission> {
        return submissionService.findByProblemId(problemId)
    }

    data class SubmissionRequest(
        val problemId: String,
        val snippet: String,
        val language: Language,
    )

    data class SubmissionResponse(
        val submissionId: String,
        val language: Language,
        val snippet: String,
        val status: SubmissionStatus,
        val runtime: Double?,
        val memory: Double?,
        val createdAt: Instant,
        val error: String?,
        val failedCase: Case?,
    ) {
        data class Case(
            val status: ExecutionCaseStatus,
            val input: String,
            val expected: String,
            val actual: String,
            val stdOut: String?,
            val stdErr: String?,
        )
    }

}
