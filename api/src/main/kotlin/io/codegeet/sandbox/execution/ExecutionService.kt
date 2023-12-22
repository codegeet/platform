package io.codegeet.sandbox.execution

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.github.dockerjava.api.async.ResultCallback
import com.github.dockerjava.api.model.Frame
import com.github.dockerjava.api.model.HostConfig
import com.github.dockerjava.api.model.StreamType
import com.github.dockerjava.core.DefaultDockerClientConfig
import com.github.dockerjava.core.DockerClientImpl
import com.github.dockerjava.zerodep.ZerodepDockerHttpClient
import io.codegeet.sandbox.model.ExecutionRequest
import io.codegeet.sandbox.model.ExecutionResponse
import io.codegeet.sandbox.model.Language
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import java.io.PipedInputStream
import java.io.PipedOutputStream
import java.net.URI
import java.time.Duration
import java.time.Instant
import java.util.*
import java.util.concurrent.TimeUnit

@Service
class ExecutionService(
    private val executionRepository: ExecutionRepository
) {

    fun execute(request: ExecutionRequest): ExecutionResponse {
        val executionId = UUID.randomUUID().toString()

        executionRepository.save(
            Execution(
                executionId = executionId,
                code = request.code,
                languageId = request.languageId,
                stdOut = null,
                stdErr = null,
                error = null,
                exitCode = null,
                createdAt = Instant.now()
            )
        )

        return if (request.sync == true) {
            execute(executionId)
            val execution = executionRepository.findByIdOrNull(executionId)

            ExecutionResponse(executionId = executionId, execution = execution)
        } else {
            Thread { execute(executionId) }.start()

            ExecutionResponse(executionId = executionId, execution = null)
        }
    }

    private fun execute(executionId: String) {
        val execution = executionRepository.findByIdOrNull(executionId) ?: throw ResponseStatusException(
            HttpStatus.NOT_FOUND,
            "Execution '$executionId' does not exist."
        )

        val containerInput = ContainerInput(
            languageId = execution.languageId,
            code = execution.code
        )

        val containerOutput = executeCmd(
            "codegeet/${execution.languageId.getId()}:latest",
            jacksonObjectMapper().writeValueAsString(containerInput)
        )

        val executedAt = Instant.now()

        executionRepository.save(
            execution.copy(
                stdOut = containerOutput.stdOut,
                stdErr = containerOutput.stderr,
                error = containerOutput.error,
                executedAt = executedAt,
                totalExecutionMillis = executedAt.toEpochMilli() - execution.createdAt.toEpochMilli(),
                exitCode = if (containerOutput.stderr.isNotEmpty()) 1 else 0
            )
        )
    }

    private fun executeCmd(imageName: String, stdin: String): ContainerOutput {
        val hostConfig: HostConfig = HostConfig.newHostConfig()
            .withMemory(128000000L)
            .withCpuPeriod(100000)
            .withCpuQuota(50000)
            .withNetworkMode("none")

        val config = DefaultDockerClientConfig.createDefaultConfigBuilder()
            .withDockerHost("unix:///var/run/docker.sock")
            .build()

        val dockerHttpClient = ZerodepDockerHttpClient.Builder()
            .dockerHost(URI("unix:///var/run/docker.sock"))
            .maxConnections(30)
            .connectionTimeout(Duration.ofSeconds(15))
            .responseTimeout(Duration.ofSeconds(30))
            .build()

        val dockerClient = DockerClientImpl.getInstance(config, dockerHttpClient)

        val container = dockerClient.createContainerCmd(imageName)
            .withHostConfig(hostConfig)
            .withTty(true)
            .withStdinOpen(true)
            .withUser("codegeet")
            .withWorkingDir("/home/codegeet")
            .exec()

        dockerClient.startContainerCmd(container.id).exec()

        val execCmd = dockerClient.execCreateCmd(container.id)
            .withAttachStdout(true)
            .withAttachStdin(true)
            .withAttachStderr(true)
            .withCmd("java", "-jar", "coderunner.jar")
            .exec()

        val execStartCmd = dockerClient.execStartCmd(execCmd.id)

        val inputStream = PipedInputStream()
        val outputStream = PipedOutputStream(inputStream)
        val stdinWriter = outputStream.bufferedWriter()

        stdinWriter.write(stdin)
        stdinWriter.newLine()
        stdinWriter.newLine() // #6 empty line is EOT signal
        stdinWriter.flush()
        stdinWriter.close()

        val containerCallback = MyResultCallback()

        execStartCmd.withStdIn(inputStream).exec(containerCallback).awaitCompletion(5, TimeUnit.SECONDS)

        dockerClient.stopContainerCmd(container.id).exec()
        dockerClient.removeContainerCmd(container.id).exec()

        return ContainerOutput(
            stdOut = containerCallback.getStdOut(),
            stderr = containerCallback.getStdErr(),
            error = ""
        )
    }

    class MyResultCallback : ResultCallback.Adapter<Frame>() {
        private val stdOutBuilder = StringBuilder()
        private val stdErrBuilder = StringBuilder()

        override fun onNext(frame: Frame) {
            when (frame.streamType) {
                StreamType.STDOUT, StreamType.RAW -> stdOutBuilder.append(String(frame.payload))
                StreamType.STDERR -> stdErrBuilder.append(String(frame.payload))
                else -> {}
            }
        }

        override fun onError(throwable: Throwable?) {
            super.onError(throwable)
        }

        fun getStdOut(): String = stdOutBuilder.toString()
        fun getStdErr(): String = stdErrBuilder.toString()
    }

    fun get(executionId: String): Execution? = executionRepository.findByIdOrNull(executionId)

    data class ContainerInput(
        @JsonProperty("language_id")
        val languageId: Language,
        val code: String
        //todo add stdin
        //todo add command
    )

    data class ContainerInputFile(
        var name: String,
        var content: String,
    )

    data class ContainerOutput(
        @JsonProperty("std_out")
        val stdOut: String,
        @JsonProperty("std_err")
        val stderr: String,
        val error: String,
    )
}
