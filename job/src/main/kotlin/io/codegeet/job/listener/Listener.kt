package io.codegeet.job.listener

import io.codegeet.job.JobService
import io.codegeet.job.config.QueueConfiguration.Companion.RPC_QUEUE_NAME
import io.codegeet.platform.common.ExecutionRequest
import io.codegeet.platform.common.ExecutionResult
import mu.KotlinLogging
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component

@Component
@ConditionalOnProperty(name = ["app.job.enabled"], havingValue = "true", matchIfMissing = true)
class Listener(
    private val executionJobService: JobService
) {
    private val logger = KotlinLogging.logger {}

    @RabbitListener(queues = [RPC_QUEUE_NAME])
    fun receive(message: ExecutionRequest): ExecutionResult {
        logger.info("Queue received message for: ${message.language}")
        return executionJobService.execute(message)
    }
}
