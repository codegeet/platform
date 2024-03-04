package io.codegeet.job

import io.codegeet.common.ExecutionJobRequest
import io.codegeet.common.ExecutionJobResult
import io.codegeet.job.code.ExecutionService
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
    fun post(@RequestBody request: ExecutionJobRequest): ExecutionJobResult {
        return executionService.execute(request)
    }
}

