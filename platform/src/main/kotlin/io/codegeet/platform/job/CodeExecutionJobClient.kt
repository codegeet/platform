package io.codegeet.platform.job

import io.codegeet.common.CodeExecutionJobRequest
import io.codegeet.common.CodeExecutionJobResult
import io.codegeet.platform.config.Configuration
import org.apache.commons.logging.LogFactory
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.stereotype.Service

@Service
class CodeExecutionJobClient(
    private val rabbitTemplate: RabbitTemplate
) {
    private val log = LogFactory.getLog(javaClass)

    fun call(request: CodeExecutionJobRequest): CodeExecutionJobResult {
        val codeExecutionJobResult = rabbitTemplate.convertSendAndReceive(
            Configuration.QUEUE_NAME,
            request
        ) as CodeExecutionJobResult

        log.info(codeExecutionJobResult.status)

        return codeExecutionJobResult
    }
}
