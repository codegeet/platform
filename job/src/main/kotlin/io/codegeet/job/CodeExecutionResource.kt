package io.codegeet.job

import io.codegeet.common.CodeExecutionJobRequest
import io.codegeet.common.CodeExecutionJobResult
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseBody

@Controller
@RequestMapping("api/executions")
class CodeExecutionResource(
    private val executionService: CodeExecutionService
) {

    @PostMapping
    @ResponseBody
    fun post(@RequestBody request: CodeExecutionJobRequest): CodeExecutionJobResult {
        return executionService.execute(request)
    }
}
