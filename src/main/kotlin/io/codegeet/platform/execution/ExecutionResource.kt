package io.codegeet.platform.execution

import io.codegeet.platform.execution.model.Execution
import io.codegeet.platform.execution.model.ExecutionRequest
import io.codegeet.platform.execution.model.ExecutionResponse
import org.springframework.web.bind.annotation.*

@RestController
class ExecutionResource(
    private val executionService: ExecutionService
) {

    @PostMapping("api/execution")
    fun execute(
        @RequestHeader("Authorization") authorization: String?,
        @RequestBody request: ExecutionRequest
    ): ExecutionResponse {

        return executionService.handleExecutionRequest(request)
    }

    @GetMapping("api/execution/{execution_id}")
    fun getExecution(
        @RequestHeader("Authorization") authorization: String?,
        @PathVariable("execution_id") executionId: String
    ): Execution {

        return executionService.getExecution(executionId)
    }

}
