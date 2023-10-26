package io.codegeet.sandbox.execution

import io.codegeet.sandbox.auth.AuthService
import io.codegeet.sandbox.model.ExecutionRequest
import io.codegeet.sandbox.model.ExecutionResponse
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException

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

        return executionService.execute(request)
    }

    @GetMapping("/execution/{execution_id}")
    fun getExecution(
        @RequestHeader("Authorization") authorization: String?,
        @PathVariable("execution_id") executionId: String
    ): Execution {
        auth.getToken(authorization)

        return executionService.get(executionId) ?: throw ResponseStatusException(
            HttpStatus.NOT_FOUND,
            "Execution '$executionId' does not exist."
        )
    }

}
