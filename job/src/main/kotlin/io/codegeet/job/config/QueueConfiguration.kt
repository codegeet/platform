package io.codegeet.job.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.springframework.amqp.core.Queue
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory
import org.springframework.amqp.rabbit.connection.ConnectionFactory
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class QueueConfiguration() {

    companion object {
        const val RPC_QUEUE_NAME: String = "rpc_queue"
    }

    @Bean
    fun queue(): Queue {
        return Queue(RPC_QUEUE_NAME, false)
    }

    @Bean
    fun jackson2JsonMessageConverter(): Jackson2JsonMessageConverter =
        Jackson2JsonMessageConverter(ObjectMapper().registerModule(KotlinModule.Builder().build()))

    @Bean
    fun rabbitListenerContainerFactory(connectionFactory: ConnectionFactory) =
        SimpleRabbitListenerContainerFactory().also {
            it.setConnectionFactory(connectionFactory)
            it.setMessageConverter(jackson2JsonMessageConverter())
        }
}
