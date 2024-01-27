package io.codegeet.platform.submission.api

import io.codegeet.platform.config.Language
import io.codegeet.platform.submission.SubmissionService
import io.codegeet.platform.submission.api.SubmissionResponse.*
import io.codegeet.platform.submission.data.Submission
import io.codegeet.platform.submission.data.ExecutionStatus
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*

@Controller
@RequestMapping("api/submissions")
class SubmissionController(
    private val submissionService: SubmissionService
) {

    @PostMapping
    @ResponseBody
    fun post(@RequestBody request: SubmissionRequest): SubmissionResponse {
        return submissionService.handle(request).toResponse()
    }

    @GetMapping("/{submission_id}")
    @ResponseBody
    fun get(@PathVariable("submission_id") submissionId: String): SubmissionResponse {
        return submissionService.getSubmission(submissionId).toResponse()
    }

    private fun Submission.toResponse() = SubmissionResponse(
        submissionId = this.submissionId,
        status = this.status,
        error = this.error,
        time = this.totalTime,
        executions = this.executions.map {
            ExecutionOutput(
                status = it.status,
                stdOut = it.stdOut,
                stdErr = it.stdErr
            )
        }
    )
}

data class SubmissionRequest(
    val code: String,
    val language: Language,
    val executions: List<ExecutionInput> = emptyList(),
    val sync: Boolean? = false,
) {
    data class ExecutionInput(
        val stdIn: String?,
        val args: List<String>?
    )
}

data class SubmissionResponse(
    val submissionId: String,
    val status: ExecutionStatus,
    val time: Long?,
    val error: String?,
    val executions: List<ExecutionOutput>?
) {

    data class ExecutionOutput(
        val status: ExecutionStatus,
        val stdOut: String?,
        val stdErr: String?
    )
}
