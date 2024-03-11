package io.codegeet.code.executions.model

import io.codegeet.common.ExecutionJobStatus
import io.codegeet.common.ExecutionJobInvocationStatus
import io.codegeet.common.Language
import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(name = "executions")
data class Execution(
    @Id
    val executionId: String,
    @Enumerated(EnumType.STRING)
    val language: Language,
    @Column(columnDefinition = "TEXT")
    val code: String,
    @Enumerated(EnumType.STRING)
    val status: ExecutionJobStatus?,

    @Column(columnDefinition = "TEXT")
    var error: String? = null,

    @OneToMany(cascade = [CascadeType.ALL], orphanRemoval = true)
    val invocations: MutableList<Invocation> = mutableListOf(),

    val createdAt: Instant,
    val totalTime: Int? = null,
)

@Entity
@Table(name = "execution_invocations")
data class Invocation(
    @Id
    val invocationId: String,
    val executionId: String,
    @Enumerated(EnumType.STRING)
    var status: ExecutionJobInvocationStatus?,
    val arguments: String?,
    @Column(columnDefinition = "TEXT")
    val stdIn: String?,
    @Column(columnDefinition = "TEXT")
    val stdOut: String? = null,
    @Column(columnDefinition = "TEXT")
    val stdErr: String? = null,
    val runtime: Long? = null,
    val memory: Long? = null,
)
