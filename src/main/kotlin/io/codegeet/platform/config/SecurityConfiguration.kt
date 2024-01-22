import io.codegeet.platform.auth.ApiKeyAuthFilter
import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class SecurityConfiguration(val apiKeyAuthFilter: ApiKeyAuthFilter) {

    @Bean
    fun apiKeyFilter(): FilterRegistrationBean<ApiKeyAuthFilter> {
        val registration = FilterRegistrationBean<ApiKeyAuthFilter>()
        registration.filter = apiKeyAuthFilter
        registration.addUrlPatterns("/api/*")
        registration.order = 1
        return registration
    }
}
