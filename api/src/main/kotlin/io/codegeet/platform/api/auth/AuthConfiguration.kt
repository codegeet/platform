package io.codegeet.platform.api.auth

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
@EnableConfigurationProperties(AuthConfiguration.ApiKeyConfig::class)
class AuthConfiguration(private val authInterceptor: AuthInterceptor) : WebMvcConfigurer {

    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(authInterceptor).addPathPatterns("/**")
    }

    @ConfigurationProperties(prefix = "app.api")
    data class ApiKeyConfig(
        val key: String
    )
}
