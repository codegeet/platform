package io.codegeet.problems.executions.resource

import io.codegeet.common.Language
import io.codegeet.problems.executions.ExecutionService
import io.codegeet.problems.executions.model.Execution
import io.codegeet.problems.executions.model.ExecutionCaseStatus
import io.codegeet.problems.executions.model.ExecutionStatus
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseBody

@Controller
@RequestMapping("api/executions")
class ExecutionResource(
    private val executionService: ExecutionService
) {

    @PostMapping
    @ResponseBody
    fun test(@RequestBody request: ExecutionRequest): ExecutionResponse {
        return executionService.execute(request).toResponse()
    }

    data class ExecutionRequest(
        val problemId: String,
        val snippet: String,
        val language: Language,
        val cases: List<Case>,
    ) {
        data class Case(
            val input: String,
            val expected: String?,
        )
    }

    data class ExecutionResponse(
        val executionId: String,
        val status: ExecutionStatus,
        val cases: List<Case>,
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

    private fun Execution.toResponse() = ExecutionResponse(
        executionId = this.executionId,
        status = this.status,
        cases = this.cases.map {
            ExecutionResponse.Case(
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
