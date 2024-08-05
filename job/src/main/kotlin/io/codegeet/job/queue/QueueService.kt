package io.codegeet.job.queue

import io.codegeet.job.ExecutionService
import io.codegeet.job.config.QueueConfiguration.ReplyQueueConfig
import io.codegeet.platform.common.ExecutionJobReply
import io.codegeet.platform.common.ExecutionJobRequest
import io.codegeet.platform.common.ExecutionResult
import mu.KLogging
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service

@Service
@ConditionalOnProperty(name = ["app.job.enabled"], havingValue = "true", matchIfMissing = true)
class QueueService(
    private val executionService: ExecutionService,
    private val replyTemplate: RabbitTemplate,
    private val replyQueueConfig: ReplyQueueConfig
) {
    companion object : KLogging() {
        const val RECEIVE_METHOD_NAME = "receive"
    }

    fun receive(message: ExecutionJobRequest) {
        logger.info("Received execution request for executionId: ${message.executionId}")
        send(message.executionId, executionService.execute(message.request))
    }

    private fun send(executionId: String, result: ExecutionResult) {
        replyTemplate.convertAndSend(
            replyQueueConfig.exchange,
            replyQueueConfig.routingKey,
            ExecutionJobReply(executionId, result)
        )
    }
}
