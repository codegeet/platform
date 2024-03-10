package io.codegeet.problems.executions.resource

import io.codegeet.common.Language
import io.codegeet.problems.executions.model.Submission
import io.codegeet.problems.submissions.SubmissionService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("api/submissions")
class SubmissionResource(
    private val service: SubmissionService
) {

    @PostMapping
    fun submit(@RequestBody request: SubmissionRequest): Submission {
        return service.submit(request)
    }

    @GetMapping
    fun getByProblemId(@RequestParam problemId: String): List<Submission> {
        return service.findByProblemId(problemId)
    }

    data class SubmissionRequest(
        val problemId: String,
        val snippet: String,
        val language: Language,
    )
}
