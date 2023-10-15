package io.codegeet.sandbox

import io.codegeet.sandbox.model.Execution
import io.codegeet.sandbox.model.ExecutionRequest
import io.codegeet.sandbox.model.ExecutionResponse
import org.springframework.web.bind.annotation.*

@RestController
class ExecutionController(
    private val service: ExecutionService
) {

    @PostMapping("/execution")
    fun execute(@RequestBody request: ExecutionRequest): ExecutionResponse {

        val executionId = service.execute(request);

        return ExecutionResponse(executionId)
    }

    @GetMapping("/execution/{execution_id}")
    fun getExecution(@PathVariable("execution_id") executionId: String): Execution {
        return service.get(executionId)
    }

}
