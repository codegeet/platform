package io.codegeet.sandbox.execution

import com.fasterxml.jackson.annotation.JsonProperty
import com.github.dockerjava.api.model.Frame
import com.github.dockerjava.core.DefaultDockerClientConfig
import com.github.dockerjava.core.DockerClientImpl
import com.github.dockerjava.core.InvocationBuilder.AsyncResultCallback
import com.github.dockerjava.zerodep.ZerodepDockerHttpClient
import io.codegeet.sandbox.model.ExecutionRequest
import io.codegeet.sandbox.model.ExecutionResponse
import io.codegeet.sandbox.model.Language
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import java.io.ByteArrayInputStream
import java.net.URI
import java.time.Duration
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.function.Consumer


@Service
class ExecutionService(
    private val executionRepository: ExecutionRepository
) {

    fun execute(request: ExecutionRequest): ExecutionResponse {

        val execution = Execution(
            executionId = UUID.randomUUID().toString(),
            code = request.code,
            languageId = request.languageId,
            stdOut = null,
            stdErr = null,
            error = null,
            exitCode = null
        )

        executionRepository.save(execution)

        Thread {
            executeAsync(execution.executionId)
        }.start()

        return ExecutionResponse(executionId = execution.executionId.toString())
    }

    fun executeAsync(executionId: String) {

        val execution = executionRepository.findByIdOrNull(executionId) ?: throw ResponseStatusException(
            HttpStatus.NOT_FOUND,
            "Execution '$executionId' does not exist."
        )

        val fileName = when (execution.languageId) {
            Language.JAVA -> "main.java"
            Language.PYTHON -> "main.py"
        }

        val containerInput = ContainerInput(
            languageId = execution.languageId,
            files = arrayOf(ContainerInputFile(name = fileName, content = execution.code))
        )

//        val config = DefaultDockerClientConfig.createDefaultConfigBuilder().build()
//
//        val dockerHttpClient = ZerodepDockerHttpClient.Builder()
//            .maxConnections(100)
//            .connectionTimeout(Duration.ofSeconds(30))
//            .responseTimeout(Duration.ofSeconds(45))
//            .build()
//
//        val dockerClient = DockerClientImpl.getInstance(config, dockerHttpClient)
//
//
//        // Create and start a Docker container
//        val container = dockerClient.createContainerCmd("codegeet/${execution.languageId.getId()}:latest")
//            .exec()
//
//        dockerClient.startContainerCmd(container.id)
//            .withAttachStdin(true)
//            .withAttachStdout(true)
//            .exec()
//
//        // Execute a command in the container and read output
//        val execCreateResponse = dockerClient.execCreateCmd(container.id)
//            .withAttachStdin(true)
//            .withAttachStdout(true)
//            .withCmd("java")
//            .exec()
//
//        val execStartResponse = dockerClient.execStartCmd(execCreateResponse.id)
//            .withDetach(false)
//            .withTty(false)
//            .exec(ExecStartResultCallback(System.out, System.err))
//
//        execStartResponse.awaitCompletion()

        // Stop and remove the container when done
//        dockerClient.stopContainerCmd(container.id).exec()
//        dockerClient.removeContainerCmd(container.id).exec()

        /* val process = ProcessBuilder(
             "docker run --rm -i -u codegeet -w /home/codegeet codegeet/${execution.languageId.getId()}:latest"
                 .split(" ")
         )
             .start()

         val stdin = process.outputStream
         val writer = BufferedWriter(OutputStreamWriter(stdin))

         writer.write(jacksonObjectMapper().writeValueAsString(containerInput));
         writer.flush();
         writer.close();

         val exitCode = process.waitFor()

         executionRepository.save(execution.let {
             val output =
                 jacksonObjectMapper().readValue(process.inputStream.readAsText(), ContainerOutput::class.java)

             it.copy(
                 stdOut = output.stdOut,
                 stdErr = output.stderr,
                 error = process.errorStream.readAsText(), //todo container error
                 exitCode = exitCode
             )
         })*/
    }

    //https://github.com/PeggyPro/Jpom/blob/b3a750ab4578e38ef5b430b5856bde8f7356b63d/modules/sub-plugin/docker-cli/src/main/java/org/dromara/jpom/DefaultDockerPluginImpl.java#L251

    //https://github.com/segiddins/misk/blob/9531b9cbafede740bc98f6e65fba32ef4ca61de7/misk-policy-testing/src/main/kotlin/misk/policy/opa/LocalOpaService.kt#L100
    fun execCmd(imageName: String) {

        val config = DefaultDockerClientConfig.createDefaultConfigBuilder()
            .withDockerHost("unix:///var/run/docker.sock")
            .build()

        val dockerHttpClient = ZerodepDockerHttpClient.Builder()
            .dockerHost(URI("unix:///var/run/docker.sock"))
            .maxConnections(100)
            .connectionTimeout(Duration.ofSeconds(30))
            .responseTimeout(Duration.ofSeconds(45))
            .build()

        val dockerClient = DockerClientImpl.getInstance(config, dockerHttpClient)

        //create container
        val container = dockerClient.createContainerCmd(imageName)
            .withAttachStdout(true)
            .withAttachStdin(true)
            .withAttachStderr(true)
            .withTty(true)
            .withStdInOnce(true)
            .withStdinOpen(true)
            .withUser("codegeet")
            .withWorkingDir("/home/codegeet")
            .exec()

        try {

            dockerClient.startContainerCmd(container.id).exec()

            val st = """{                               
  "language_id": "java",
  "code": "class Main {    public static void main(String[] args) {    System.out.print(\"Hello World!!!\"); }   }"
}
"""
            val input = ByteArrayInputStream(st.toByteArray(Charsets.UTF_8))

            dockerClient.attachContainerCmd(container.id)
                .withStdIn(input)
                .withStdOut(true)
                .withStdErr(true)
                .withFollowStream(true)

                .exec(object : AsyncResultCallback<Frame>() {
                    override fun onNext(frame: Frame) {


                        println("Received frame: " + String(frame.payload, Charsets.UTF_8));
                    }

                    override fun onError(throwable: Throwable) {
                        System.err.println("Error: " + throwable.message)
                    }
                }).awaitCompletion(5, TimeUnit.SECONDS)

            input.close()

            dockerClient.removeContainerCmd(container.id).withForce(true).exec()
            dockerClient.close()

        } catch (e: InterruptedException) {

        } finally {
        }
    }

    fun execCmd2(imageName: String) {

        val config = DefaultDockerClientConfig.createDefaultConfigBuilder()
            .withDockerHost("unix:///var/run/docker.sock")
            .build()

        val dockerHttpClient = ZerodepDockerHttpClient.Builder()
            .dockerHost(URI("unix:///var/run/docker.sock"))
            .maxConnections(100)
            .connectionTimeout(Duration.ofSeconds(30))
            .responseTimeout(Duration.ofSeconds(45))
            .build()

        val dockerClient = DockerClientImpl.getInstance(config, dockerHttpClient)


        //create container
        val container = dockerClient.createContainerCmd(imageName)
            .withAttachStdout(true)
            .withAttachStdin(true)
            .withAttachStderr(true)
            .withTty(true)
            .withStdInOnce(true)
            .withStdinOpen(true)
            .withUser("codegeet")
            .withWorkingDir("/home/codegeet")
            .exec()

        try {

            dockerClient.startContainerCmd(container.id).exec()


            val st = """{                               
  "language_id": "java",
  "code": "class Main {    public static void main(String[] args) {    System.out.print(\"Hello World!!!\"); }   }"
}
"""

            val input = ByteArrayInputStream(st.toByteArray(Charsets.UTF_8))

            val execCreateCmd = dockerClient.execCreateCmd(container.id)
            execCreateCmd
                .withAttachStdout(true)
                .withAttachStdin(true)
                .withAttachStderr(true)
                .withTty(false)
                .withCmd("java", "-jar", "coderunner.jar")

            val exec = execCreateCmd.exec()

            val execId = exec.id
            val execStartCmd = dockerClient.execStartCmd(execId)
            execStartCmd.withDetach(false).withTty(true).withStdIn(input)

            execStartCmd.exec(object : AsyncResultCallback<Frame>() {
                override fun onNext(frame: Frame) {
                    println("Received frame: " + String(frame.payload, Charsets.UTF_8));
                }

                override fun onError(throwable: Throwable) {
                    System.err.println("Error: " + throwable.message)
                }
            }).awaitCompletion(5, TimeUnit.SECONDS)

            dockerClient.removeContainerCmd(container.id).withForce(true).exec()

            input.close()
            dockerClient.close()

        } catch (e: InterruptedException) {
            // errorConsumer.accept("容器cli被中断:$e")
        } finally {
            //  errorConsumer.accept("exit")
        }
    }

    class StringConsumer : Consumer<String> {
        override fun accept(t: String) {
            // Implement your logic here
            println("Consumed: $t")
        }
    }

    fun get(executionId: String): Execution? = executionRepository.findByIdOrNull(executionId)

    data class ContainerInput(
        @JsonProperty("language_id")
        val languageId: Language,
        val files: Array<ContainerInputFile>
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
