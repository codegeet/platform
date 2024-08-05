package io.codegeet.job.config

import io.codegeet.job.queue.QueueService
import org.springframework.amqp.core.Binding
import org.springframework.amqp.core.BindingBuilder
import org.springframework.amqp.core.DirectExchange
import org.springframework.amqp.core.Queue
import org.springframework.amqp.rabbit.connection.ConnectionFactory
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter
import org.springframework.amqp.support.converter.MessageConverter
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@ConditionalOnProperty(name = ["app.job.enabled"], havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(
    value = [
        QueueConfiguration.RequestQueueConfig::class,
        QueueConfiguration.ReplyQueueConfig::class
    ]
)
class QueueConfiguration {

    // handle requests

    @Bean(name = ["requestQueue"])
    fun requestQueue(config: RequestQueueConfig): Queue {
        return Queue(config.name, false)
    }

    @Bean
    fun simpleMessageListenerContainer(
        connectionFactory: ConnectionFactory,
        messageListenerAdapter: MessageListenerAdapter,
        config: RequestQueueConfig
    ) = SimpleMessageListenerContainer()
        .also {
            it.setConnectionFactory(connectionFactory)
            it.setQueueNames(config.name)
            it.setMessageListener(messageListenerAdapter)
            if (config.consumers != null) {
                it.setConcurrentConsumers(config.consumers)
            }
        }

    @Bean
    fun messageListenerAdapter(receiver: QueueService, messageConverter: MessageConverter): MessageListenerAdapter {
        val adapter = MessageListenerAdapter(receiver, QueueService.RECEIVE_METHOD_NAME)
        adapter.setMessageConverter(messageConverter)
        return adapter
    }

    // handle replies

    @Bean(name = ["replyQueue"])
    fun replyQueue(config: ReplyQueueConfig): Queue {
        return Queue(config.name, false)
    }

    @Bean
    fun replyExchange(config: ReplyQueueConfig): DirectExchange {
        return DirectExchange(config.exchange)
    }

    @Bean(name = ["replyBinding"])
    fun replyBinding(
        @Qualifier("replyQueue") queue: Queue,
        exchange: DirectExchange,
        config: ReplyQueueConfig
    ): Binding {
        return BindingBuilder.bind(queue).to(exchange).with(config.routingKey)
    }

    @Bean
    fun producerTemplate(connectionFactory: ConnectionFactory, messageConverter: MessageConverter): RabbitTemplate {
        val template = RabbitTemplate(connectionFactory)
        template.messageConverter = messageConverter
        return template
    }

    @ConfigurationProperties(prefix = "app.job.queue.request")
    data class RequestQueueConfig(
        val name: String,
        val consumers: Int? = null,
    )

    @ConfigurationProperties(prefix = "app.job.queue.reply")
    data class ReplyQueueConfig(
        val name: String,
        val exchange: String,
        val routingKey: String,
    )
}
