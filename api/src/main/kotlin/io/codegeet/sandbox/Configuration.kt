package io.codegeet.sandbox

import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.github.dockerjava.api.DockerClient
import com.github.dockerjava.core.DefaultDockerClientConfig
import com.github.dockerjava.core.DockerClientImpl
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient
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

    // todo Configure Docker Client similar to https://github.com/bmuschko/gradle-docker-plugin/blob/e5abdc28e483905bacd49ae49738482dbd567a60/src/main/java/com/bmuschko/gradle/docker/internal/services/DockerClientService.java#L75
    @Bean
    fun dockerClient(): DockerClient {
        val config = DefaultDockerClientConfig.createDefaultConfigBuilder().build()

        val dockerHttpClient = ApacheDockerHttpClient.Builder()
            .dockerHost(config.dockerHost)
            .sslConfig(config.sslConfig)
            .maxConnections(50)
            .connectionTimeout(Duration.ofSeconds(15))
            .responseTimeout(Duration.ofSeconds(30))
            .build()

        val dockerClient = DockerClientImpl.getInstance(config, dockerHttpClient)

        try {
            dockerClient.pingCmd().exec()
        } catch (e: Exception) {
            throw RuntimeException("Docker client initialization failed. Docker host: '${config.dockerHost}'.", e)
        }

        return dockerClient
    }

    @Bean
    fun apiConfiguration(): ContainerConfiguration {
        return ContainerConfiguration()
    }
}
