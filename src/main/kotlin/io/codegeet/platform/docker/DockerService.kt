package io.codegeet.platform.docker

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.github.dockerjava.api.DockerClient
import com.github.dockerjava.api.async.ResultCallback
import com.github.dockerjava.api.model.Frame
import com.github.dockerjava.api.model.HostConfig
import com.github.dockerjava.api.model.StreamType
import io.codegeet.platform.config.DockerConfiguration
import org.springframework.stereotype.Service
import java.io.PipedInputStream
import java.io.PipedOutputStream
import java.util.concurrent.TimeUnit

@Service
class DockerService(
    private val dockerClient: DockerClient,
    private val configuration: DockerConfiguration.DockerContainerConfiguration
) {

    fun exec(image: String, input: String): ExecutionOutput {
        val containerId = createContainer(image)

        val callback = ContainerCallback()

        val outputStream = PipedOutputStream()
        val inputStream = PipedInputStream(outputStream)

        val attachContainerCallback = attachContainer(containerId, inputStream, callback)
        startContainer(containerId)

        outputStream.write("$input\n\n".toByteArray())
        outputStream.flush()
        outputStream.close()

        attachContainerCallback.awaitCompletion(15, TimeUnit.SECONDS)
        attachContainerCallback.close()

        stopContainer(containerId)
        removeContainer(containerId)

        return buildExecutionOutput(callback)
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
            // do nothing
        }
    }

    private fun createContainer(image: String): String {
        val hostConfig = HostConfig.newHostConfig()
            .withMemory(configuration.memory)
            .withNetworkMode("none")

        return dockerClient.createContainerCmd(image)
            .withStdinOpen(true)
            .withStdInOnce(true)
            .withHostConfig(hostConfig)
            .exec().id
    }

    private fun buildExecutionOutput(callback: ContainerCallback): ExecutionOutput {
        val out = callback.getStdOut()
            .takeIf { it.isNotEmpty() }
            ?.let { jacksonObjectMapper().readValue(it, ExecutionOutput::class.java) }
            ?: ExecutionOutput(stdOut = "", stdErr = "", error = "No stdout from container.")

        val err = callback.getStdErr().ifEmpty { out.stdErr }

        return ExecutionOutput(stdOut = out.stdOut, stdErr = err, error = out.error)
    }

    private class ContainerCallback : ResultCallback.Adapter<Frame>() {
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
            stdErrBuilder.append("Error during container execution.")
        }

        fun getStdOut(): String = stdOutBuilder.toString()
        fun getStdErr(): String = stdErrBuilder.toString()
    }

    data class ExecutionOutput(
        @JsonProperty("std_out")
        val stdOut: String,
        @JsonProperty("std_err")
        val stdErr: String,
        @JsonProperty("error")
        val error: String,
        @JsonProperty("exec_code")
        val execCode: Int? = null,
        @JsonProperty("exec_time")
        val execMillis: Long? = null,
    )
}
