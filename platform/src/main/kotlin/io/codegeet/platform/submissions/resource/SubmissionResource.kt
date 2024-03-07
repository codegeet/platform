package io.codegeet.platform.submissions.resource

import io.codegeet.common.Language
import io.codegeet.platform.submissions.SubmissionService
import io.codegeet.platform.submissions.model.Submission
import io.codegeet.platform.submissions.model.SubmissionCaseStatus
import io.codegeet.platform.submissions.model.SubmissionStatus
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*

@Controller
@RequestMapping("api/submissions")
class SubmissionResource(
    private val submissionService: SubmissionService
) {

    @PostMapping
    @ResponseBody
    fun submit(@RequestBody request: SubmissionRequest): SubmissionResponse {
        return submissionService.submit(request).toResponse()
    }

    @PostMapping("/test")
    @ResponseBody
    fun test(@RequestBody request: SubmissionRequest): SubmissionResponse {
        return submissionService.test(request).toResponse()
    }

    @GetMapping("/{submission_id}")
    @ResponseBody
    fun get(@PathVariable("submission_id") submissionId: String): SubmissionResponse {
        return submissionService.getSubmission(submissionId).toResponse()
    }

    data class SubmissionRequest(
        val problemId: String,
        val snippet: String,
        val language: Language,
        val inputs: List<String>
    )

    data class SubmissionResponse(
        val submissionId: String,
        val status: SubmissionStatus,
        val cases: List<Case>,
    ) {
        data class Case(
            val status: SubmissionCaseStatus,
            val input: String,
            val expected: String,
            val actual: String,
            val stdOut: String,
            val stdErr: String,
        )
    }

    private fun Submission.toResponse() = SubmissionResponse(
        submissionId = this.submissionId,
        status = this.status,
        cases = this.cases.map {
            SubmissionResponse.Case(
                status = it.status,
                input = it.input,
                expected = it.expected,
                actual = it.actual,
                stdOut = it.stdOut,
                stdErr = it.stdErr
            )
        }
    )
}
