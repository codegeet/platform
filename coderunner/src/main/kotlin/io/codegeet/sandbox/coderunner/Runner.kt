package io.codegeet.sandbox.coderunner

import io.codegeet.sandbox.coderunner.model.ApplicationInput
import io.codegeet.sandbox.coderunner.model.ApplicationOutput
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption

class Runner(private val languages: Languages) {

    fun run(
        input: ApplicationInput
    ): ApplicationOutput {

        val properties = languages.getSettingsFor(input.languageId)

        // todo add --path argument
        val directory = System.getProperty("user.dir").orEmpty()
        writeFiles(input.code, directory, properties.fileName)

        // build
        properties.build?.let { command ->
            val process = ProcessBuilder(command.split(" "))
            .redirectError(ProcessBuilder.Redirect.INHERIT)
                .directory(File(directory))
                .start()

            val code = process.waitFor()

            if (code != 0) {
                return ApplicationOutput(
                    stdOut = "",
                    stdErr = process.inputStream.readAsText(),
                    error = process.errorStream.readAsText().also { print(it) },
                )
            }
        }

        // execute
        properties.run.let { command ->
            val process = ProcessBuilder(command.split(" "))
                .directory(File(directory))
                .start()

            process.waitFor()

            return ApplicationOutput(
                stdOut = process.inputStream.readAsText(),
                stdErr = process.errorStream.readAsText(),
                error = ""
            )
        }
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
