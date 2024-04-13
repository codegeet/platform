package io.codegeet.platform.api.job

import io.codegeet.platform.common.ExecutionRequest
import io.codegeet.platform.common.ExecutionResult
import io.codegeet.platform.api.config.Configuration
import org.apache.commons.logging.LogFactory
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.stereotype.Service

@Service
class ExecutionJobClient(
    private val rabbitTemplate: RabbitTemplate
) {
    private val log = LogFactory.getLog(javaClass)

    fun call(request: ExecutionRequest): ExecutionResult {
        val executionResult = rabbitTemplate.convertSendAndReceive(
            Configuration.QUEUE_NAME,
            request
        ) as ExecutionResult

        return executionResult
    }
}
