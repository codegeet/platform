package io.codegeet.platform.config

import com.fasterxml.jackson.module.kotlin.KotlinModule
import io.codegeet.platform.auth.ApiKey
import io.codegeet.platform.auth.ApiKeyRepository
import io.codegeet.platform.exceptions.MissingDefaultApiKeyException
import org.springframework.boot.ApplicationRunner
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment

@Configuration
class Configuration(private val environment: Environment) {

    @Bean
    fun jackson2ObjectMapperBuilderCustomizer(): Jackson2ObjectMapperBuilderCustomizer {
        return Jackson2ObjectMapperBuilderCustomizer { builder ->
            builder.modulesToInstall(KotlinModule.Builder().build())
        }
    }

    @Bean
    fun dbInit(apiKeyRepository: ApiKeyRepository) = ApplicationRunner {
        environment.getProperty("DEFAULT_API_KEY")?.let { apiKey ->
            apiKeyRepository.save(ApiKey(apiKey, "Default API key", true))
        } ?: throw MissingDefaultApiKeyException("Missing default api key.")
    }
}
