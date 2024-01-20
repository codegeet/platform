package io.codegeet.sandbox.execution

import io.codegeet.sandbox.container.ContainerService
import io.codegeet.sandbox.execution.exceptions.ExecutionNotFoundException
import io.codegeet.sandbox.execution.model.Execution
import io.codegeet.sandbox.execution.model.ExecutionRequest
import io.codegeet.sandbox.execution.model.ExecutionResponse
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.*

@Service
class ExecutionService(
    private val executionRepository: ExecutionRepository,
    private val containerService: ContainerService
) {

    fun handleExecutionRequest(request: ExecutionRequest): ExecutionResponse {
        val executionId = UUID.randomUUID().toString()

        executionRepository.save(
            Execution(
                executionId = executionId,
                code = request.code,
                language = request.language,
                stdOut = null,
                stdErr = null,
                error = null,
                exitCode = null,
                createdAt = Instant.now()
            )
        )

        return if (request.sync == true) {
            execute(executionId)
            ExecutionResponse(executionId = executionId, execution = getExecution(executionId))
        } else {
            Thread { execute(executionId) }.start()
            ExecutionResponse(executionId = executionId, execution = null)
        }
    }

    fun getExecution(executionId: String): Execution = executionRepository.findByIdOrNull(executionId)
        ?: throw ExecutionNotFoundException("Execution '$executionId' not found.")

    private fun execute(executionId: String) {
        val execution = getExecution(executionId)

        val coderunnerOutput = containerService.run(execution.language, execution.code)

        val executedAt = Instant.now()
        executionRepository.save(
            execution.copy(
                stdOut = coderunnerOutput?.stdOut,
                stdErr = coderunnerOutput?.stdErr,
                error = coderunnerOutput?.error,
                executedAt = executedAt,
                totalExecutionMillis = executedAt.toEpochMilli() - execution.createdAt.toEpochMilli(),
                exitCode = if (coderunnerOutput?.stdErr?.isNotEmpty() == true) 1 else 0
            )
        )
    }
}
