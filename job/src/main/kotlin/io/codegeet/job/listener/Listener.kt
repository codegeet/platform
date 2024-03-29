package io.codegeet.job.listener

import io.codegeet.common.ExecutionJobRequest
import io.codegeet.common.ExecutionJobResult
import io.codegeet.job.ExecutionJobService
import io.codegeet.job.config.QueueConfiguration.Companion.RPC_QUEUE_NAME
import org.apache.commons.logging.LogFactory
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.stereotype.Component

@Component
class Listener(
    private val executionJobService: ExecutionJobService
) {
    private val log = LogFactory.getLog(javaClass)

    @RabbitListener(queues = [RPC_QUEUE_NAME])
    fun receive(message: ExecutionJobRequest): ExecutionJobResult {
        log.debug("RPC queue received message")
        return executionJobService.execute(message)
    }
}
