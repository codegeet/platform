package io.codegeet.platform.exceptions

class ExecutionNotFoundException(executionId: String) : RuntimeException("Execution: $executionId not found")
