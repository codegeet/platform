package io.codegeet.sandbox.execution

import io.codegeet.sandbox.auth.AuthService
import io.codegeet.sandbox.execution.model.Execution
import io.codegeet.sandbox.execution.model.ExecutionRequest
import io.codegeet.sandbox.execution.model.ExecutionResponse
import org.springframework.web.bind.annotation.*

@RestController
class ExecutionController(
    private val auth: AuthService,
    private val executionService: ExecutionService
) {

    @PostMapping("/execution")
    fun execute(
        @RequestHeader("Authorization") authorization: String?,
        @RequestBody request: ExecutionRequest
    ): ExecutionResponse {
        auth.getToken(authorization)

        return executionService.handleExecutionRequest(request)
    }

    @GetMapping("/execution/{execution_id}")
    fun getExecution(
        @RequestHeader("Authorization") authorization: String?,
        @PathVariable("execution_id") executionId: String
    ): Execution {
        auth.getToken(authorization)

        return executionService.getExecution(executionId)
    }

}
