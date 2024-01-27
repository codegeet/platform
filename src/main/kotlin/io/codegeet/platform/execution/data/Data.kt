package io.codegeet.platform.execution.data

import io.codegeet.platform.config.Language
import io.codegeet.platform.execution.api.ExecutionStatus
import io.codegeet.platform.execution.api.ExecutionType
import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(name = "executions")
data class Execution(
    @Id
    val executionId: String,
    @Enumerated(EnumType.STRING)
    val type: ExecutionType,
    @Enumerated(EnumType.STRING)
    val language: Language,
    @Column(columnDefinition = "TEXT")
    val code: String,
    val createdAt: Instant,
    @Enumerated(EnumType.STRING)
    var status: ExecutionStatus,
    var error: String? = null,
    var totalTime: Long? = null,

    @OneToMany(cascade = [CascadeType.ALL], orphanRemoval = true)
    val executions: MutableList<ExecutionInputOutput> = mutableListOf(),
)

@Entity
@Table(name = "executions_input_output")
data class ExecutionInputOutput(
    @Id
    val executionInputOutputId: String,
    @ManyToOne(fetch = FetchType.LAZY)
    val execution: Execution,
    @Enumerated(EnumType.STRING)
    var status: ExecutionStatus,
    val input: String?, // json
    var result: String? = null,
    val args: String?,
    @Column(columnDefinition = "TEXT")
    val stdIn: String?,
    @Column(columnDefinition = "TEXT")
    var stdOut: String? = null,
    @Column(columnDefinition = "TEXT")
    var stdErr: String? = null,
    @Column(columnDefinition = "TEXT")
    var error: String? = null
)
