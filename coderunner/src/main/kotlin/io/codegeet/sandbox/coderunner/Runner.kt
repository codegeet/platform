package io.codegeet.sandbox.coderunner

import io.codegeet.sandbox.coderunner.model.ApplicationInput
import io.codegeet.sandbox.coderunner.model.ApplicationOutput
import java.io.File
import java.lang.System.currentTimeMillis
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption

class Runner {

    fun run(
        input: ApplicationInput
    ): ApplicationOutput {

        val directory = try {
            val directory = getUserHomeDirectory()
            writeFiles(input.code, directory, input.fileName)

            directory
        } catch (e: Exception) {
            return ApplicationOutput(error = "Something went wrong during the build: ${e.message}")
        }

        // build
        input.instructions.build
            ?.takeIf { it.isNotEmpty() }
            ?.let { command ->
                try {
                    val process = ProcessBuilder(command.split(" "))
                        .directory(File(directory))
                        .start()

                    val code = process.waitFor()

                    if (code != 0) {
                        return ApplicationOutput(
                            stdErr = process.inputStream.readAsText(),
                            error = process.errorStream.readAsText(),
                        )
                    }
                } catch (e: Exception) {
                    return ApplicationOutput(error = "Something went wrong during the build: ${e.message}")
                }
            }

        // execute
        input.instructions.exec
            .takeIf { it.isNotEmpty() }
            ?.let { command ->
                try {
                    val processBuilder = ProcessBuilder(command.split(" ") + input.args.orEmpty())
                        .directory(File(directory))

                    val startMillis = currentTimeMillis()

                    val process = processBuilder.start()
                    val code = process.waitFor()

                    val endMillis = currentTimeMillis()

                    return ApplicationOutput(
                        stdOut = process.inputStream.readAsText(),
                        stdErr = process.errorStream.readAsText(),
                        execCode = code,
                        execMillis = endMillis - startMillis,
                    )
                } catch (e: Exception) {
                    return ApplicationOutput(error = e.message ?: "Something went wrong during the exec: ${e.message}")
                }
            }
            ?: return ApplicationOutput(error = "Execution should not be empty.")
    }

    private fun getUserHomeDirectory(): String {
        return System.getProperty("user.home")
    }

    private fun writeFiles(
        content: String,
        directory: String,
        fileName: String
    ) {
        val path = Path.of(directory, fileName)

        Files.write(
            path,
            content.toByteArray(),
            StandardOpenOption.CREATE,
            StandardOpenOption.TRUNCATE_EXISTING
        )
    }
}
