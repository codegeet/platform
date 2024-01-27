package io.codegeet.platform.submission

import com.fasterxml.jackson.databind.ObjectMapper
import io.codegeet.platform.config.Language
import io.codegeet.platform.config.LanguageConfiguration
import io.codegeet.platform.docker.DockerInput
import io.codegeet.platform.docker.DockerOutput
import io.codegeet.platform.docker.DockerService
import io.codegeet.platform.exceptions.ExecutionNotFoundException
import io.codegeet.platform.submission.api.SubmissionRequest
import io.codegeet.platform.submission.data.Submission
import io.codegeet.platform.submission.data.Execution
import io.codegeet.platform.submission.data.ExecutionStatus
import io.codegeet.platform.submission.data.SubmissionRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*

@Service
class SubmissionService(
    private val submissionRepository: SubmissionRepository,
    private val dockerClient: DockerService,
    private val languageConfiguration: LanguageConfiguration,
    private val objectMapper: ObjectMapper,
    private val clock: Clock,
) {

    fun handle(request: SubmissionRequest): Submission {
        val submission = request.toSubmission(Instant.now(clock).truncatedTo(ChronoUnit.MILLIS))

        submissionRepository.save(submission)

        return if (request.sync == true) {
            execute(submission.submissionId)
            getSubmission(submission.submissionId)
        } else {
            Thread { execute(submission.submissionId) }.start()
            submission
        }
    }

    fun getSubmission(submissionId: String): Submission = submissionRepository.findByIdOrNull(submissionId)
        ?: throw ExecutionNotFoundException("Submission '$submissionId' not found.")

    private fun execute(submissionId: String) {
        val submission = getSubmission(submissionId)
        val executions = submission.executions.map {
            DockerInput.ExecutionInput(
                args = it.args?.split(" ").orEmpty(),
                stdIn = it.stdIn
            )
        }

        val output = execute(submission.language, submission.code, executions)

        submission.totalTime = Duration.between(submission.createdAt, Instant.now()).toMillis()
        submission.error = output.error
        submission.status = if (output.execCode == 1 || output.executions.any { it.execCode == 1 })
            ExecutionStatus.FAILED
        else
            ExecutionStatus.COMPLETED

        submission.executions.forEachIndexed { i, it ->
            val out = output.executions[i]
            it.stdOut = out.stdOut
            it.stdErr = out.stdErr
            it.status = if (out.execCode == 1) ExecutionStatus.FAILED else ExecutionStatus.COMPLETED
        }

        submissionRepository.save(submission)
    }

    fun execute(language: Language, code: String, executions: List<DockerInput.ExecutionInput>): DockerOutput {
        val languageSettings = languageConfiguration.getSettingsFor(language)

        val input = DockerInput(
            code = code,
            fileName = languageSettings.fileName,
            instructions = DockerInput.Instructions(
                compile = languageSettings.compile,
                exec = languageSettings.exec
            ),
            executions = executions
        )

        val imageName = "codegeet/${language.getId()}:latest"

        return dockerClient.exec(imageName, objectMapper.writeValueAsString(input))
    }

    fun SubmissionRequest.toSubmission(now: Instant) = Submission(
        submissionId = UUID.randomUUID().toString(),
        code = this.code,
        language = this.language,
        status = ExecutionStatus.NOT_STARTED,
        createdAt = now
    ).also { execution ->
        execution.executions.addAll(this.executions.takeIf { it.isNotEmpty() }
            ?.map {
                Execution(
                    executionId = UUID.randomUUID().toString(),
                    submission = execution,
                    status = ExecutionStatus.NOT_STARTED,
                    args = it.args?.joinToString(" "),
                    stdIn = it.stdIn,
                )
            } ?: listOf(
            Execution(
                executionId = UUID.randomUUID().toString(),
                submission = execution,
                status = ExecutionStatus.NOT_STARTED,
                args = null,
                stdIn = null,
            )
        ))
    }
}
