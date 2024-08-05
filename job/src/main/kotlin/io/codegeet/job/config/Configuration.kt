package io.codegeet.job.config

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder
import java.time.Clock


@Configuration
class Configuration() {

    @Bean
    fun objectMapperBuilder(): ObjectMapper = Jackson2ObjectMapperBuilder.json()
        .propertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
        .serializationInclusion(JsonInclude.Include.NON_NULL)
        .modulesToInstall(KotlinModule.Builder().build())
        .build()

    @Bean
    fun messageConverter(objectMapper: ObjectMapper): Jackson2JsonMessageConverter =
        Jackson2JsonMessageConverter(ObjectMapper().registerModule(KotlinModule.Builder().build()))

    @Bean
    fun clock(): Clock = Clock.systemUTC()
}
