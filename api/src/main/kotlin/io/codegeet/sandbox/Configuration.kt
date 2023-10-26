package io.codegeet.sandbox

import com.fasterxml.jackson.module.kotlin.KotlinModule
import io.codegeet.sandbox.auth.Token
import io.codegeet.sandbox.auth.TokenRepository
import org.springframework.boot.ApplicationRunner
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class Configuration {

    @Bean
    fun jackson2ObjectMapperBuilderCustomizer(): Jackson2ObjectMapperBuilderCustomizer {
        return Jackson2ObjectMapperBuilderCustomizer { builder ->
            builder.modulesToInstall(KotlinModule.Builder().build())
        }
    }

    @Bean
    fun databaseInitializer(tokenRepository: TokenRepository) = ApplicationRunner {
        tokenRepository.save(Token("00000000-aaa-bbb-ccc-123456789000"))
    }
}
