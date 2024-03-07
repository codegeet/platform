package io.codegeet.platform.executions

import io.codegeet.common.CodeExecutionJobRequest
import io.codegeet.platform.job.CodeExecutionJobClient
import io.codegeet.platform.exceptions.ExecutionNotFoundException
import io.codegeet.platform.executions.model.CodeExecutionRepository
import io.codegeet.platform.executions.model.Execution
import io.codegeet.platform.executions.model.Invocation
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.time.Clock
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID

@Service
class CodeExecutionService(
    private val codeExecutionRepository: CodeExecutionRepository,
    private val codeExecutionJobClient: CodeExecutionJobClient,
    private val clock: Clock,
) {
    fun execute(request: CodeExecutionResource.CodeExecutionRequest, sync: Boolean = false): Execution {
        val execution = codeExecutionRepository.save(toExecution(request))

        return if (sync) {
            execute(execution)
        } else {
            // todo do something better ;)
            Thread { execute(execution) }.start()
            execution
        }
    }

    fun getExecution(executionId: String): Execution = codeExecutionRepository.findByIdOrNull(executionId)
        ?: throw ExecutionNotFoundException(executionId)

    private fun execute(execution: Execution): Execution {
        return codeExecutionJobClient.call(execution.toExecutionJobRequest())
            .let { response ->
                codeExecutionRepository.save(
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

    private fun toExecution(request: CodeExecutionResource.CodeExecutionRequest) = request.toExecution(
        executionId = UUID.randomUUID().toString(),
        now = Instant.now(clock).truncatedTo(ChronoUnit.MILLIS)
    )

    private fun CodeExecutionResource.CodeExecutionRequest.toExecution(executionId: String, now: Instant) = Execution(
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

    private fun Execution.toExecutionJobRequest() =
        CodeExecutionJobRequest(
            code = this.code,
            language = this.language,
            invocations = this.invocations.map {
                CodeExecutionJobRequest.InvocationRequest(
                    arguments = it.arguments?.split("\n"),
                    stdIn = it.stdIn
                )
            },
            stats = true
        )
}
