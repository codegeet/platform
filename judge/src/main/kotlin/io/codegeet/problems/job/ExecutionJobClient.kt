package io.codegeet.problems.job

import io.codegeet.common.ExecutionJobRequest
import io.codegeet.common.ExecutionJobResult
import io.codegeet.problems.config.Configuration
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

        log.info(executionJobResult.status)

        return executionJobResult
    }
}
