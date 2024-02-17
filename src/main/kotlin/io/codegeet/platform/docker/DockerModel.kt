package io.codegeet.platform.docker

data class DockerInput(
    val code: String,
    val executions: List<ExecutionInput>,
    val fileName: String,
    val instructions: Instructions
) {
    data class ExecutionInput(
        val args: List<String>?,
        val stdIn: String?
    )

    data class Instructions(
        val compile: String,
        val exec: String,
    )
}

data class DockerOutput(
    val executions: List<ExecutionOutput> = emptyList(),
    val error: String,
    val execCode: Int,
) {
    data class ExecutionOutput(
        val stdOut: String,
        val stdErr: String,
        val execCode: Int,
    )
}
