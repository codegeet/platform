package io.codegeet.platform.executions

import io.codegeet.common.ExecutionJobRequest
import io.codegeet.platform.executions.exceptions.ExecutionNotFoundException
import io.codegeet.platform.executions.model.Execution
import io.codegeet.platform.executions.model.Invocation
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.time.Clock
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID

@Service
class ExecutionService(
    private val executionRepository: ExecutionRepository,
    private val executionJobClient: ExecutionJobClient,
    private val clock: Clock,
) {
    fun execute(request: ExecutionController.ExecutionRequest, sync: Boolean = false): Execution {
        val execution = executionRepository.save(toExecution(request))

        return if (sync) {
            execute(execution)
        } else {
            // todo do something better ;)
            Thread { execute(execution) }.start()
            execution
        }
    }

    fun getExecution(executionId: String): Execution = executionRepository.findByIdOrNull(executionId)
        ?: throw ExecutionNotFoundException(executionId)

    private fun execute(execution: Execution): Execution {
        return executionJobClient.call(execution.toExecutionJobRequest())
            .let { response ->
                executionRepository.save(
                    execution.copy(
                        status = response.status,
                        error = response.error,
                        invocations = execution.invocations.mapIndexed { i, invocation ->
                            response.invocations.getOrNull(i)?.let {
                                invocation.copy(
                                    status = it.status,
                                    stdOut = it.stdOut,
                                    stdErr = it.stdErr
                                )
                            } ?: invocation.copy(
                                status = null,
                                stdErr = "Not found in output"
                            )
                        }.toMutableList()
                    )
                )
            }
    }

    private fun toExecution(request: ExecutionController.ExecutionRequest) = request.toExecution(
        executionId = UUID.randomUUID().toString(),
        now = Instant.now(clock).truncatedTo(ChronoUnit.MILLIS)
    )

    private fun ExecutionController.ExecutionRequest.toExecution(executionId: String, now: Instant) = Execution(
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
                    arguments = it.arguments?.joinToString(" "),
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

    private fun Execution.toExecutionJobRequest() =
        ExecutionJobRequest(
            code = this.code,
            language = this.language,
            invocations = this.invocations.map {
                ExecutionJobRequest.InvocationRequest(
                    arguments = it.arguments?.split("\n"),
                    stdIn = it.stdIn
                )
            },
            stats = true
        )
}
