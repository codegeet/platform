package io.codegeet.job.code

import com.fasterxml.jackson.databind.ObjectMapper
import io.codegeet.common.ExecutionJobRequest
import io.codegeet.common.ExecutionJobResult
import io.codegeet.job.docker.DockerService
import org.springframework.stereotype.Service

@Service
class ExecutionService(
    private val dockerClient: DockerService,
    private val objectMapper: ObjectMapper,
) {
    fun execute(request: ExecutionJobRequest): ExecutionJobResult {
        val imageName = "codegeet/${request.language.getId()}:latest"

        return dockerClient.exec(imageName, objectMapper.writeValueAsString(request))
    }
}
