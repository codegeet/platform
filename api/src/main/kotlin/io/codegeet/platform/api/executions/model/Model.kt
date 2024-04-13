package io.codegeet.platform.api.executions.model

import io.codegeet.platform.common.ExecutionStatus
import io.codegeet.platform.common.InvocationStatus
import io.codegeet.platform.common.language.Language
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant

@Document(collection = "executions")
data class Execution(
    @Id
    val executionId: String,
    val language: Language,
    val code: String,
    val status: ExecutionStatus?,
    var error: String? = null,

    val invocations: MutableList<Invocation> = mutableListOf(),

    val createdAt: Instant,
    val totalTime: Int? = null,
)

data class Invocation(
    val executionId: String,
    var status: InvocationStatus?,
    val arguments: String?,
    val stdIn: String?,
    val stdOut: String? = null,
    val stdErr: String? = null,
    val runtime: Long? = null,
    val memory: Long? = null,
)
