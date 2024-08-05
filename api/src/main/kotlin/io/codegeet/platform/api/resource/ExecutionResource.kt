package io.codegeet.platform.api.resource

import io.codegeet.platform.api.executions.ExecutionService
import io.codegeet.platform.api.executions.model.Execution
import io.codegeet.platform.common.ExecutionStatus
import io.codegeet.platform.common.InvocationStatus
import io.codegeet.platform.common.language.Language
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*

@Controller
@RequestMapping("api/executions")
class ExecutionResource(private val executionService: ExecutionService) {

    @PostMapping
    @ResponseBody
    fun post(@RequestBody body: ExecutionRequest): ExecutionResponse {
        return executionService.execute(body.toRequest()).toResponse()
    }

    @GetMapping("{executionId}")
    @ResponseBody
    fun get(@PathVariable executionId: String): ExecutionResponse {
        return executionService.get(executionId).toResponse()
    }

    data class ExecutionRequest(
        val code: String,
        val language: Language,
        val invocations: List<InvocationInput> = emptyList(),
    ) {
        data class InvocationInput(
            val stdIn: String? = null,
            val args: List<String>? = null,
        )
    }

    data class ExecutionResponse(
        val executionId: String,
        val status: ExecutionStatus?,
        val time: Int?,
        val error: String?,
        val invocations: List<InvocationOutput>? = null
    ) {
        data class InvocationOutput(
            val status: InvocationStatus?,
            val details: InvocationDetails?,
            val stdOut: String?,
            val stdErr: String?
        )

        data class InvocationDetails(
            val runtime: Long?,
            val memory: Long?,
        )
    }

    private fun ExecutionRequest.toRequest() = io.codegeet.platform.common.ExecutionRequest(
        code = this.code,
        language = this.language,
        invocations = this.invocations.map {
            io.codegeet.platform.common.ExecutionRequest.InvocationRequest(
                args = it.args,
                stdIn = it.stdIn
            )
        }
    )

    private fun Execution.toResponse() = ExecutionResponse(
        executionId = this.executionId,
        status = this.status,
        error = this.error,
        time = this.totalTime,
        invocations = this.invocations
            .filter { status != null }
            .map {
                ExecutionResponse.InvocationOutput(
                    status = it.status,
                    stdOut = it.stdOut,
                    stdErr = it.stdErr,
                    details = ExecutionResponse.InvocationDetails(runtime = it.runtime, memory = it.memory)
                )
            }
            .ifEmpty { null }
    )
}
