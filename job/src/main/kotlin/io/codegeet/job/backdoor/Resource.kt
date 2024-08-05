package io.codegeet.job.backdoor

import io.codegeet.job.ExecutionService
import io.codegeet.platform.common.ExecutionRequest
import io.codegeet.platform.common.ExecutionResult
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseBody

@Controller
@RequestMapping("api/executions")
class Resource(private val service: ExecutionService) {

    @PostMapping
    @ResponseBody
    fun post(@RequestBody request: ExecutionRequest): ExecutionResult {
        return service.execute(request)
    }
}
