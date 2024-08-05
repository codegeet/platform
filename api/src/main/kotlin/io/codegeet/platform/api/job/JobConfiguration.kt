package io.codegeet.platform.api.job

import org.springframework.amqp.core.Binding
import org.springframework.amqp.core.BindingBuilder
import org.springframework.amqp.core.DirectExchange
import org.springframework.amqp.core.Queue
import org.springframework.amqp.rabbit.connection.ConnectionFactory
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter
import org.springframework.amqp.support.converter.MessageConverter
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(
    value = [
        JobConfiguration.RequestQueueConfig::class,
        JobConfiguration.ReplyQueueConfig::class
    ]
)
class JobConfiguration {

    // submit requests to execution job

    @Bean
    fun producerQueue(config: RequestQueueConfig): Queue {
        return Queue(config.name, false)
    }

    @Bean
    fun producerExchange(config: RequestQueueConfig): DirectExchange {
        return DirectExchange(config.exchange)
    }

    @Bean
    fun producerBinding(producerQueue: Queue, producerExchange: DirectExchange, config: RequestQueueConfig): Binding {
        return BindingBuilder.bind(producerQueue).to(producerExchange).with(config.routingKey)
    }

    @Bean
    fun producerTemplate(connectionFactory: ConnectionFactory, jsonMessageConverter: MessageConverter): RabbitTemplate {
        val template = RabbitTemplate(connectionFactory)
        template.messageConverter = jsonMessageConverter
        return template
    }

    // process execution job responses

    @Bean
    fun consumerQueue(config: ReplyQueueConfig): Queue {
        return Queue(config.name, false)
    }

    @Bean
    fun simpleMessageListenerContainer(
        connectionFactory: ConnectionFactory,
        messageListenerAdapter: MessageListenerAdapter,
        config: ReplyQueueConfig
    ) = SimpleMessageListenerContainer().also {
        it.setConnectionFactory(connectionFactory)
        it.setQueueNames(config.name)
        it.setMessageListener(messageListenerAdapter)
    }

    @Bean
    fun messageListenerAdapter(
        receiver: JobClient,
        messageConverter: MessageConverter
    ): MessageListenerAdapter {
        val adapter = MessageListenerAdapter(receiver, JobClient.RECEIVE_METHOD_NAME)
        adapter.setMessageConverter(messageConverter)
        return adapter
    }

    @ConfigurationProperties(prefix = "app.job.queue.reply")
    data class ReplyQueueConfig(
        val name: String
    )

    @ConfigurationProperties(prefix = "app.job.queue.request")
    data class RequestQueueConfig(
        val name: String,
        val exchange: String,
        val routingKey: String,
    )
}
