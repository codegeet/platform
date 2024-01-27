package io.codegeet.platform.submission.data

import io.codegeet.platform.config.Language
import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(name = "submissions")
data class Submission(
    @Id
    val submissionId: String,
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
    val executions: MutableList<Execution> = mutableListOf(),
)

@Entity
@Table(name = "executions")
data class Execution(
    @Id
    val executionId: String,
    @ManyToOne(fetch = FetchType.LAZY)
    val submission: Submission,
    @Enumerated(EnumType.STRING)
    var status: ExecutionStatus,
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

enum class ExecutionStatus {
    NOT_STARTED, COMPLETED, FAILED
}
