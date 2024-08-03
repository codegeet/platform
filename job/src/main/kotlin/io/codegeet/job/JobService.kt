package io.codegeet.job

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.dockerjava.api.DockerClient
import com.github.dockerjava.api.async.ResultCallback
import com.github.dockerjava.api.command.PullImageResultCallback
import com.github.dockerjava.api.exception.NotFoundException
import com.github.dockerjava.api.model.Frame
import com.github.dockerjava.api.model.HostConfig
import com.github.dockerjava.api.model.StreamType
import io.codegeet.job.config.DockerConfiguration.DockerConfig
import io.codegeet.platform.common.ExecutionRequest
import io.codegeet.platform.common.ExecutionResult
import io.codegeet.platform.common.ExecutionStatus
import io.codegeet.platform.common.language.Language
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import mu.KotlinLogging
import org.springframework.stereotype.Service
import java.io.PipedInputStream
import java.io.PipedOutputStream
import java.nio.channels.ClosedByInterruptException
import java.util.concurrent.TimeUnit

private val logger = KotlinLogging.logger {}

@Service
class JobService(
    private val dockerClient: DockerClient,
    private val config: DockerConfig,
    private val objectMapper: ObjectMapper
) {

    @OptIn(DelicateCoroutinesApi::class)
    fun execute(request: ExecutionRequest): ExecutionResult {
        return runCatching {
            val containerId = createContainer(getImageName(request.language))

            val callback = ContainerCallback(containerId)

            val outputStream = PipedOutputStream()
            val inputStream = PipedInputStream(outputStream)

            val attachContainerCallback = attachContainer(containerId, inputStream, callback)
            startContainer(containerId)

            outputStream.write("${objectMapper.writeValueAsString(request)}\n\n".toByteArray())
            outputStream.flush()
            outputStream.close()

            attachContainerCallback.awaitCompletion(config.timeoutSeconds, TimeUnit.SECONDS)
            attachContainerCallback.close()

            GlobalScope.launch {
                stopContainer(containerId)
                removeContainer(containerId)
            }

            buildExecutionResult(callback)
        }.getOrElse { e ->
            when (e) {
                is NotFoundException -> {
                    GlobalScope.launch { pull(request.language) }

                    ExecutionResult(
                        status = ExecutionStatus.INTERNAL_ERROR,
                        error = "Docker image ${request.language} is not found."
                    )
                }

                else -> {
                    ExecutionResult(
                        status = ExecutionStatus.INTERNAL_ERROR,
                        error = "Docker container failure ${e.message}"
                    )
                }
            }
        }
    }

    private fun pull(language: Language) {
        dockerClient.pullImageCmd(getImageName(language)).exec(PullImageResultCallback()).awaitCompletion()
    }

    private fun attachContainer(
        containerId: String,
        containerInputStream: PipedInputStream,
        containerCallback: ContainerCallback
    ): ContainerCallback {
        return dockerClient.attachContainerCmd(containerId)
            .withStdOut(true)
            .withStdErr(true)
            .withStdIn(containerInputStream)
            .withFollowStream(true)
            .exec(containerCallback)
    }

    private fun startContainer(containerId: String) {
        dockerClient.startContainerCmd(containerId).exec()
    }

    private fun removeContainer(containerId: String) {
        dockerClient.removeContainerCmd(containerId).exec()
    }

    private fun stopContainer(containerId: String) {
        try {
            dockerClient.stopContainerCmd(containerId).exec()
        } catch (e: Exception) {
            // logger.debug("Failed to stop container: $containerId")
        }
    }

    private fun createContainer(image: String): String {
        val hostConfig = HostConfig.newHostConfig()
            .withMemory(config.memory)
            .withNetworkMode("none")

        return dockerClient.createContainerCmd(image)
            .withStdinOpen(true)
            .withStdInOnce(true)
            .withHostConfig(hostConfig)
            .exec()
            .also { response ->
                response.warnings.forEach { logger.warn(it) }
            }.id
    }

    private fun buildExecutionResult(callback: ContainerCallback): ExecutionResult = try {
        callback.getStdOut()
            .takeIf { it.isNotEmpty() }
            ?.let { objectMapper.readValue(it, ExecutionResult::class.java) }
            ?: ExecutionResult(
                status = ExecutionStatus.INTERNAL_ERROR,
                error = callback.getStdErr().takeIf { it.isNotEmpty() } ?: "No stdout from container")
    } catch (e: Exception) {
        ExecutionResult(
            status = ExecutionStatus.INTERNAL_ERROR,
            error = "Failed to parse container output: ${callback.getStdOut()}"
        )
    }

    private class ContainerCallback(val containerId: String) : ResultCallback.Adapter<Frame>() {
        private val stdOutBuilder = StringBuilder()
        private val stdErrBuilder = StringBuilder()

        override fun onNext(frame: Frame) {
            when (frame.streamType) {
                StreamType.STDOUT, StreamType.RAW -> {
                    String(frame.payload).let {
                        stdOutBuilder.append(it)
                    }
                }

                StreamType.STDERR -> {
                    String(frame.payload).let {
                        logger.error { "STDERR $containerId: $it" }
                        stdErrBuilder.append(it)
                    }
                }

                StreamType.STDIN -> {
                    String(frame.payload).let {
                        // log
                    }
                }

                else -> {}
            }
        }

        override fun onError(throwable: Throwable) {
            if (throwable is ClosedByInterruptException)
                "Container may have been stopped by timeout".let {
                    logger.debug { it }
                    stdErrBuilder.append(it)
                }
            else
                "Error during container execution: ${throwable.message ?: "unknown"}".let {
                    logger.debug { it }
                    stdErrBuilder.append(it)
                }
        }

        fun getStdOut(): String = stdOutBuilder.toString()
        fun getStdErr(): String = stdErrBuilder.toString()
    }

    private fun getImageName(language: Language) = "codegeet/${language.getId()}:latest"
}
