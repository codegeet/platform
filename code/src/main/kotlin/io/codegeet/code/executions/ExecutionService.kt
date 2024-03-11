package io.codegeet.code.executions

import io.codegeet.common.ExecutionJobRequest
import io.codegeet.code.executions.model.Execution
import io.codegeet.code.executions.model.ExecutionRepository
import io.codegeet.code.executions.model.Invocation
import io.codegeet.code.job.ExecutionJobClient
import org.springframework.stereotype.Service
import java.time.Clock
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*

@Service
class ExecutionService(
    private val executionRepository: ExecutionRepository,
    private val jobClient: ExecutionJobClient,
    private val clock: Clock,
) {
    fun execute(request: ExecutionResource.ExecutionRequest, sync: Boolean = false): Execution {
        val execution = executionRepository.save(toExecution(request))
        return execute(execution)
    }

    private fun execute(execution: Execution): Execution {
        val result = jobClient.call(execution.toJob())

        val updatedExecution = execution.copy(
            status = result.status,
            error = result.error,
            invocations = execution.invocations.mapIndexed { i, invocation ->
                result.invocations.getOrNull(i)?.let {
                    invocation.copy(
                        status = it.status,
                        stdOut = it.stdOut,
                        stdErr = it.stdErr,
                        runtime = it.details?.duration,
                        memory = it.details?.memory
                    )
                } ?: invocation.copy(
                    status = null,
                    stdErr = "Not found in output"
                )
            }.toMutableList()
        )

        return executionRepository.save(updatedExecution)
    }

    private fun toExecution(request: ExecutionResource.ExecutionRequest) = request.toExecution(
        executionId = UUID.randomUUID().toString(),
        now = Instant.now(clock).truncatedTo(ChronoUnit.MILLIS)
    )

    private fun ExecutionResource.ExecutionRequest.toExecution(executionId: String, now: Instant) = Execution(
        executionId = executionId,
        code = this.code,
        language = this.language,
        status = null,
        createdAt = now,
    ).also { execution ->
        execution.invocations.addAll(this.invocations.takeIf { it.isNotEmpty() }
            ?.map {
                Invocation(
                    invocationId = UUID.randomUUID().toString(),
                    executionId = execution.executionId,
                    status = null,
                    arguments = it.arguments?.joinToString("\n"),
                    stdIn = it.stdIn,
                )
            } ?: listOf(
            Invocation(
                invocationId = UUID.randomUUID().toString(),
                executionId = execution.executionId,
                status = null,
                arguments = null,
                stdIn = null,
            )
        ))
    }

    private fun Execution.toJob() =
        ExecutionJobRequest(
            code = this.code,
            language = this.language,
            invocations = this.invocations.map {
                ExecutionJobRequest.InvocationRequest(
                    arguments = it.arguments?.split("\n"),
                    stdIn = it.stdIn
                )
            },
        )
}
