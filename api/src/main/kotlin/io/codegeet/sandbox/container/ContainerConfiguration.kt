package io.codegeet.sandbox.container

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "container")
data class ContainerConfiguration(
    var userName: String = "codegeet",
    var workingDir: String = "/home/codegeet",
    var memory: Long = 128000000,
    var cpuPeriod: Long = 100000,
    var cpuQuota: Long = 50000,
    var timeoutSeconds: Long = 10,
)
