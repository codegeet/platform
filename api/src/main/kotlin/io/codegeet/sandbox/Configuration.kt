package io.codegeet.sandbox

import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.github.dockerjava.api.DockerClient
import com.github.dockerjava.core.DefaultDockerClientConfig
import com.github.dockerjava.core.DockerClientImpl
import com.github.dockerjava.zerodep.ZerodepDockerHttpClient
import io.codegeet.sandbox.auth.Token
import io.codegeet.sandbox.auth.TokenRepository
import io.codegeet.sandbox.container.ContainerConfiguration
import org.springframework.boot.ApplicationRunner
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.net.URI
import java.time.Duration

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

    @Bean
    fun dockerClient(): DockerClient {
        val config = DefaultDockerClientConfig.createDefaultConfigBuilder()
            .withDockerHost("unix:///var/run/docker.sock")
            .build()

        val dockerHttpClient = ZerodepDockerHttpClient.Builder()
            .dockerHost(URI("unix:///var/run/docker.sock"))
            .maxConnections(30)
            .connectionTimeout(Duration.ofSeconds(15))
            .responseTimeout(Duration.ofSeconds(30))
            .build()

        return DockerClientImpl.getInstance(config, dockerHttpClient)
    }

    @Bean
    fun apiConfiguration(): ContainerConfiguration {
        return ContainerConfiguration()
    }
}
