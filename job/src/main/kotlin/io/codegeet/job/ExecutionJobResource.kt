package io.codegeet.job

import io.codegeet.common.ExecutionJobRequest
import io.codegeet.common.ExecutionJobResult
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseBody

@Controller
@RequestMapping("api/executions")
class ExecutionJobResource(
    private val executionJobService: ExecutionJobService
) {

    @PostMapping
    @ResponseBody
    fun post(@RequestBody request: ExecutionJobRequest): ExecutionJobResult {
        return executionJobService.execute(request)
    }
}
