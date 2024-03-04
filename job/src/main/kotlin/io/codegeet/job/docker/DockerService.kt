package io.codegeet.job.docker

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.dockerjava.api.DockerClient
import com.github.dockerjava.api.async.ResultCallback
import com.github.dockerjava.api.model.Frame
import com.github.dockerjava.api.model.HostConfig
import com.github.dockerjava.api.model.StreamType
import io.codegeet.common.ExecutionJobResult
import io.codegeet.common.ExecutionStatus
import io.codegeet.job.config.DockerConfiguration.DockerContainerConfiguration
import kotlinx.coroutines.*
import org.apache.commons.logging.LogFactory
import org.springframework.stereotype.Service
import java.io.PipedInputStream
import java.io.PipedOutputStream
import java.nio.channels.ClosedByInterruptException
import java.util.concurrent.TimeUnit

@Service
class DockerService(
    private val dockerClient: DockerClient,
    private val config: DockerContainerConfiguration,
    private val objectMapper: ObjectMapper
) {
    private val log = LogFactory.getLog(javaClass)

    @OptIn(DelicateCoroutinesApi::class)
    fun exec(image: String, input: String): ExecutionJobResult {
        return try {
            val containerId = createContainer(image)

            val callback = ContainerCallback(containerId)

            val outputStream = PipedOutputStream()
            val inputStream = PipedInputStream(outputStream)

            val attachContainerCallback = attachContainer(containerId, inputStream, callback)
            startContainer(containerId)

            outputStream.write("$input\n\n".toByteArray())
            outputStream.flush()
            outputStream.close()

            attachContainerCallback.awaitCompletion(config.timeoutSeconds, TimeUnit.SECONDS)
            attachContainerCallback.close()

            GlobalScope.launch {
                stopContainer(containerId)
                removeContainer(containerId)
            }

            buildExecutionResult(callback)
        } catch (e: Exception) {
            ExecutionJobResult(status = ExecutionStatus.INTERNAL_ERROR, error = "Docker container failure ${e.message}")
        }
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
            log.debug("Failed to stop container: $containerId")
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
            .also {
                it.warnings.forEach { log.warn(it) }
            }.id
    }

    private fun buildExecutionResult(callback: ContainerCallback): ExecutionJobResult = try {
        callback.getStdOut()
            .takeIf { it.isNotEmpty() }
            ?.let { objectMapper.readValue(it, ExecutionJobResult::class.java) }
            ?: ExecutionJobResult(
                status = ExecutionStatus.INTERNAL_ERROR,
                error = callback.getStdErr().takeIf { it.isNotEmpty() } ?: "No stdout from container")
    } catch (e: Exception) {
        ExecutionJobResult(
            status = ExecutionStatus.INTERNAL_ERROR,
            error = "Failed to parse container output: ${callback.getStdOut()}"
        )
    }

    private class ContainerCallback(val containerId: String) : ResultCallback.Adapter<Frame>() {
        private val log = LogFactory.getLog(javaClass)

        private val stdOutBuilder = StringBuilder()
        private val stdErrBuilder = StringBuilder()

        override fun onNext(frame: Frame) {
            when (frame.streamType) {
                StreamType.STDOUT, StreamType.RAW -> {
                    String(frame.payload).let {
                        log.debug("STDOUT $containerId: $it")
                        stdOutBuilder.append(it)
                    }
                }

                StreamType.STDERR -> {
                    String(frame.payload).let {
                        log.debug("STDERR $containerId: $it")
                        stdErrBuilder.append(it)
                    }
                }

                StreamType.STDIN -> {
                    String(frame.payload).let {
                        log.debug("STDIN $containerId: $it")
                    }
                }

                else -> {}
            }
        }

        override fun onError(throwable: Throwable) {
            if (throwable is ClosedByInterruptException)
                "Container may have been stopped by timeout".let {
                    log.debug(it)
                    stdErrBuilder.append(it)
                }
            else
                "Error during container execution: ${throwable.message ?: "unknown"}".let {
                    log.debug(it)
                    stdErrBuilder.append(it)
                }
        }

        fun getStdOut(): String = stdOutBuilder.toString()
        fun getStdErr(): String = stdErrBuilder.toString()
    }
}
