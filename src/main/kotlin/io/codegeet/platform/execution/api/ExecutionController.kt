package io.codegeet.platform.execution.api

import io.codegeet.platform.execution.ExecutionService
import io.codegeet.platform.execution.data.Execution
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*

@Controller
@RequestMapping("api/executions")
class ExecutionController(
    private val executionService: ExecutionService
) {

    @PostMapping
    @ResponseBody
    fun post(@RequestBody request: ExecutionRequest): ExecutionResponse {
        return executionService.handle(request).toResponse()
    }

    @GetMapping("/{execution_id}")
    @ResponseBody
    fun get(@PathVariable("execution_id") executionId: String): ExecutionResponse {
        return executionService.getExecution(executionId).toResponse()
    }

    private fun Execution.toResponse() = ExecutionResponse(
        executionId = this.executionId,
        status = this.status,
        error = this.error,
        time = this.totalTime,
        executions = this.executions.map {
            ExecutionsOutput(
                status = it.status,
                output = if (it.status == ExecutionStatus.NOT_STARTED) null else Output(
                    result = it.result,
                    stdOut = it.stdOut,
                    stdErr = it.stdErr
                )
            )
        }
    )
}




