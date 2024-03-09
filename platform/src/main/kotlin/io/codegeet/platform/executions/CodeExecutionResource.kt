package io.codegeet.platform.executions

import io.codegeet.common.ExecutionStatus
import io.codegeet.common.InvocationStatus
import io.codegeet.common.Language
import io.codegeet.platform.executions.model.Execution
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*

@Controller
@RequestMapping("api/executions")
class CodeExecutionResource(
    private val codeExecutionService: CodeExecutionService
) {

    @PostMapping
    @ResponseBody
    fun post(@RequestBody request: CodeExecutionRequest): CodeExecutionResponse {
        return codeExecutionService.execute(request).toResponse()
    }

    @GetMapping("/{execution_id}")
    @ResponseBody
    fun get(@PathVariable("execution_id") executionId: String): CodeExecutionResponse {
        return codeExecutionService.getExecution(executionId).toResponse()
    }

    private fun Execution.toResponse() = CodeExecutionResponse(
        executionId = this.executionId,
        status = this.status,
        error = this.error,
        time = this.totalTime,
        invocations = this.invocations.map {
            CodeExecutionResponse.InvocationOutput(
                status = it.status,
                stdOut = it.stdOut,
                stdErr = it.stdErr
            )
        }
    )

    data class CodeExecutionRequest(
        val code: String,
        val language: Language,
        val invocations: List<InvocationInput> = emptyList(),
    ) {
        data class InvocationInput(
            val stdIn: String? = null,
            val arguments: List<String>? = null,
        )
    }

    data class CodeExecutionResponse(
        val executionId: String,
        val status: ExecutionStatus?,
        val time: Int?,
        val error: String?,
        val invocations: List<InvocationOutput>?
    ) {
        data class InvocationOutput(
            val status: InvocationStatus?,
            val stdOut: String?,
            val stdErr: String?
        )
    }
}
