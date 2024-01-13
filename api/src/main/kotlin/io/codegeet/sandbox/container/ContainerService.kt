package io.codegeet.sandbox.container

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.github.dockerjava.api.DockerClient
import com.github.dockerjava.api.async.ResultCallback
import com.github.dockerjava.api.model.Frame
import com.github.dockerjava.api.model.HostConfig
import com.github.dockerjava.api.model.StreamType
import io.codegeet.sandbox.languages.Language
import io.codegeet.sandbox.languages.Languages
import org.springframework.stereotype.Service
import java.io.PipedInputStream
import java.io.PipedOutputStream
import java.util.concurrent.TimeUnit

@Service
class ContainerService(
    private val dockerClient: DockerClient,
    private val configuration: ContainerConfiguration,
    private val languages: Languages
) {

    fun run(language: Language, code: String): CoderunnerOutput {

        val languageSettings = languages.getSettingsFor(language)

        val coderunnerInput = CoderunnerInput(
            code = code,
            args = emptyList(),
            fileName = languageSettings.fileName,
            instructions = Instructions(
                build = languageSettings.build,
                exec = languageSettings.exec
            )
        )

        val imageName = "codegeet/${language.getId()}:latest"

        val hostConfig: HostConfig = HostConfig.newHostConfig()
            .withMemory(configuration.memory)
            .withCpuPeriod(configuration.cpuPeriod)
            .withCpuQuota(configuration.cpuQuota)
            .withNetworkMode("none")

        val container = dockerClient.createContainerCmd(imageName)
            .withHostConfig(hostConfig)
            .withTty(true)
            .withStdinOpen(true)
            .withUser(configuration.userName)
            .withWorkingDir(configuration.workingDir)
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

        stdinWriter.write(jacksonObjectMapper().writeValueAsString(coderunnerInput))
        stdinWriter.newLine()
        stdinWriter.newLine() // #6 empty line is EOT signal
        stdinWriter.flush()
        stdinWriter.close()

        val containerCallback = MyResultCallback()

        execStartCmd.withStdIn(inputStream).exec(containerCallback)
            .awaitCompletion(configuration.timeoutSeconds, TimeUnit.SECONDS)

        dockerClient.stopContainerCmd(container.id).exec()
        dockerClient.removeContainerCmd(container.id).exec()

        val coderunnerStdOutput = containerCallback.getStdOut().takeIf { it.isNotEmpty() }?.let {
            jacksonObjectMapper().readValue(
                containerCallback.getStdOut(),
                CoderunnerOutput::class.java
            )
        } ?: CoderunnerOutput(stdOut = "", stdErr = "", error = "")

        return CoderunnerOutput(
            stdOut = coderunnerStdOutput.stdOut,
            stdErr = containerCallback.getStdErr().ifEmpty { coderunnerStdOutput.stdErr },
            error = coderunnerStdOutput.error
        )
    }

    data class CoderunnerInput(
        @JsonProperty("code")
        val code: String,
        @JsonProperty("args")
        val args: List<String>?,
        @JsonProperty("fileName")
        val fileName: String,
        @JsonProperty("instructions")
        val instructions: Instructions
    )

    data class Instructions(
        @JsonProperty("build")
        val build: String? = null,
        @JsonProperty("exec")
        val exec: String,
    )

    data class CoderunnerOutput(
        @JsonProperty("std_out")
        val stdOut: String,
        @JsonProperty("std_err")
        val stdErr: String,
        @JsonProperty("error")
        val error: String,
    )

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

}