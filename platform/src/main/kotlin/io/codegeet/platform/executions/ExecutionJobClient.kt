package io.codegeet.platform.executions

import io.codegeet.common.ExecutionJobRequest
import io.codegeet.common.ExecutionJobResult
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.stereotype.Service

@Service
class ExecutionJobClient(
    private val rabbitTemplate: RabbitTemplate
) {

    fun call(request: ExecutionJobRequest): ExecutionJobResult {
        return rabbitTemplate.convertSendAndReceive(request) as ExecutionJobResult
    }
}
