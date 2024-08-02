package io.codegeet.job.listener

import io.codegeet.platform.common.ExecutionRequest
import io.codegeet.platform.common.ExecutionResult
import io.codegeet.job.JobService
import io.codegeet.job.config.QueueConfiguration.Companion.RPC_QUEUE_NAME
import org.apache.commons.logging.LogFactory
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component

@Component
@ConditionalOnProperty(name = ["app.job.enabled"], havingValue = "true", matchIfMissing = true)
class Listener(
    private val executionJobService: JobService
) {
    private val log = LogFactory.getLog(javaClass)

    @RabbitListener(queues = [RPC_QUEUE_NAME])
    fun receive(message: ExecutionRequest): ExecutionResult {
        log.debug("RPC queue received message for: ${message.language}")
        return executionJobService.execute(message)
    }
}
