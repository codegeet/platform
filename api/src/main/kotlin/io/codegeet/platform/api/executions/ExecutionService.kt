package io.codegeet.platform.api.executions

import io.codegeet.platform.api.exceptions.ExecutionNotFoundException
import io.codegeet.platform.api.executions.model.Execution
import io.codegeet.platform.api.executions.model.ExecutionRepository
import io.codegeet.platform.api.executions.model.Invocation
import io.codegeet.platform.api.job.JobClient
import io.codegeet.platform.common.ExecutionRequest
import org.springframework.stereotype.Service
import java.time.Clock
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*

@Service
class ExecutionService(
    private val executionRepository: ExecutionRepository,
    private val jobClient: JobClient,
    private val clock: Clock,
) {

    fun execute(request: ExecutionRequest, sync: Boolean = false): Execution {
        val execution = executionRepository.save(toExecution(request))

        jobClient.submit(execution.executionId, request)
        return execution
    }

    fun get(executionId: String): Execution {
        return executionRepository.findById(executionId)
            .orElseThrow { ExecutionNotFoundException("Execution with id: $executionId not found") }
    }

    private fun toExecution(request: ExecutionRequest) = request.toExecution(
        executionId = UUID.randomUUID().toString(),
        now = Instant.now(clock).truncatedTo(ChronoUnit.MILLIS)
    )

    private fun ExecutionRequest.toExecution(executionId: String, now: Instant) = Execution(
        executionId = executionId,
        code = this.code,
        language = this.language,
        status = null,
        createdAt = now,
    ).also { execution ->
        execution.invocations.addAll(this.invocations.takeIf { it.isNotEmpty() }
            ?.map {
                Invocation(
                    executionId = execution.executionId,
                    status = null,
                    arguments = it.args?.joinToString("\n"),
                    stdIn = it.stdIn,
                )
            } ?: listOf(
            Invocation(
                executionId = execution.executionId,
                status = null,
                arguments = null,
                stdIn = null,
            )
        ))
    }

    private fun Execution.toJob() =
        ExecutionRequest(
            code = this.code,
            language = this.language,
            invocations = this.invocations.map {
                ExecutionRequest.InvocationRequest(
                    args = it.arguments?.split("\n"),
                    stdIn = it.stdIn
                )
            },
        )
}
