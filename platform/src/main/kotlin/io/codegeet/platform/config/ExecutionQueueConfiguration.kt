package io.codegeet.platform.config

import io.codegeet.platform.config.ExecutionQueueConfiguration.*
import org.springframework.amqp.core.Queue
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.properties.ConfigurationProperties

import org.springframework.boot.context.properties.EnableConfigurationProperties

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(ExecutionQueueConfig::class)
class ExecutionQueueConfiguration(private val config: ExecutionQueueConfig) {

    @Bean
    fun queue(): Queue {
        return Queue(config.requestQueueName, false)
    }

    @ConfigurationProperties(prefix = "app.queue")
    data class ExecutionQueueConfig(
        @Value("\${request-queue-name}")
        val requestQueueName: String = "execution-request-queue",
        @Value("\${response-queue-name}")
        val responseQueueName: String = "execution-response-queue",
    )
}
