package io.codegeet.platform.api.job

import io.codegeet.platform.api.executions.ExecutionReplyService
import io.codegeet.platform.common.ExecutionJobReply
import io.codegeet.platform.common.ExecutionJobRequest
import io.codegeet.platform.common.ExecutionRequest
import mu.KLogging
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.stereotype.Service

@Service
class JobClient(
    private val requestTemplate: RabbitTemplate,
    private val requestQueueConfig: JobConfiguration.RequestQueueConfig,
    private val replyHandler: ExecutionReplyService,
) {

    companion object : KLogging() {
        const val RECEIVE_METHOD_NAME = "receive"
    }

    fun submit(executionId: String, request: ExecutionRequest) {
        logger.info("Submit execution request for executionId: $executionId")
        requestTemplate.convertAndSend(
            requestQueueConfig.exchange,
            requestQueueConfig.routingKey,
            ExecutionJobRequest(executionId, request)
        )
    }

    fun receive(response: ExecutionJobReply) {
        logger.info("Received execution result for executionId: ${response.executionId}")
        replyHandler.handle(response.executionId, response.result)
    }
}
