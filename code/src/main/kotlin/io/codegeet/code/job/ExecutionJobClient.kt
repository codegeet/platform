package io.codegeet.code.job

import io.codegeet.common.ExecutionJobRequest
import io.codegeet.common.ExecutionJobResult
import io.codegeet.code.config.Configuration
import org.apache.commons.logging.LogFactory
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.stereotype.Service

@Service
class ExecutionJobClient(
    private val rabbitTemplate: RabbitTemplate
) {
    private val log = LogFactory.getLog(javaClass)

    fun call(request: ExecutionJobRequest): ExecutionJobResult {
        val executionJobResult = rabbitTemplate.convertSendAndReceive(
            Configuration.QUEUE_NAME,
            request
        ) as ExecutionJobResult

        return executionJobResult
    }
}
