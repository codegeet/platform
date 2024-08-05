package io.codegeet.platform.api.executions

import io.codegeet.platform.api.executions.model.ExecutionRepository
import io.codegeet.platform.common.ExecutionResult
import org.springframework.stereotype.Service
import kotlin.jvm.optionals.getOrNull

@Service
class ExecutionReplyService(
    private val executionRepository: ExecutionRepository,
) {

    fun handle(executionId: String, result: ExecutionResult) {
        executionRepository.findById(executionId)
            .getOrNull()
            ?.let { execution ->
                executionRepository.save(
                    execution.copy(
                        status = result.status,
                        error = result.error,
                        invocations = execution.invocations.mapIndexed { i, invocation ->
                            result.invocations.getOrNull(i)?.let {
                                invocation.copy(
                                    status = it.status,
                                    stdOut = it.stdOut,
                                    stdErr = it.stdErr,
                                    runtime = it.details?.runtime,
                                    memory = it.details?.memory
                                )
                            } ?: invocation.copy(
                                status = null,
                                stdErr = "Not found in output"
                            )
                        }.toMutableList()
                    )
                )
            } ?: let {
            // log error execution is not found
        }
    }
}