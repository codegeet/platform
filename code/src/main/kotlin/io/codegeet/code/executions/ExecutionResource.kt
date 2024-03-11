package io.codegeet.code.executions

import io.codegeet.common.ExecutionJobInvocationStatus
import io.codegeet.common.ExecutionJobStatus
import io.codegeet.common.Language
import io.codegeet.code.executions.model.Execution
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
    fun post(@RequestBody request: ExecutionRequest): ExecutionResponse {
        return executionService.execute(request).toResponse()
    }

    data class ExecutionRequest(
        val code: String,
        val language: Language,
        val invocations: List<InvocationInput> = emptyList(),
    ) {
        data class InvocationInput(
            val stdIn: String? = null,
            val arguments: List<String>? = null,
        )
    }

    data class ExecutionResponse(
        val status: ExecutionJobStatus?,
        val time: Int?,
        val error: String?,
        val invocations: List<InvocationOutput>?
    ) {
        data class InvocationOutput(
            val status: ExecutionJobInvocationStatus?,
            val details: InvocationDetails?,
            val stdOut: String?,
            val stdErr: String?
        )

        data class InvocationDetails(
            val runtime: Long?,
            val memory: Long?,
        )
    }

    private fun Execution.toResponse() = ExecutionResponse(
        status = this.status,
        error = this.error,
        time = this.totalTime,
        invocations = this.invocations.map {
            io.codegeet.code.executions.ExecutionResource.ExecutionResponse.InvocationOutput(
                status = it.status,
                stdOut = it.stdOut,
                stdErr = it.stdErr,
                details = ExecutionResponse.InvocationDetails(runtime = it.runtime, memory = it.memory)
            )
        }
    )

}
