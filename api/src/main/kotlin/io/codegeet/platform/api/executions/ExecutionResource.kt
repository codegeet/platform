package io.codegeet.platform.api.executions

import io.codegeet.platform.common.InvocationStatus
import io.codegeet.platform.common.ExecutionStatus
import io.codegeet.platform.common.language.Language
import io.codegeet.platform.api.executions.model.Execution
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
            val args: List<String>? = null,
        )
    }

    data class ExecutionResponse(
        val status: ExecutionStatus?,
        val time: Int?,
        val error: String?,
        val invocations: List<InvocationOutput>?
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

    private fun Execution.toResponse() = ExecutionResponse(
        status = this.status,
        error = this.error,
        time = this.totalTime,
        invocations = this.invocations.map {
            io.codegeet.platform.api.executions.ExecutionResource.ExecutionResponse.InvocationOutput(
                status = it.status,
                stdOut = it.stdOut,
                stdErr = it.stdErr,
                details = io.codegeet.platform.api.executions.ExecutionResource.ExecutionResponse.InvocationDetails(runtime = it.runtime, memory = it.memory)
            )
        }
    )

}
