package io.codegeet.platform.executions.exceptions

class ExecutionNotFoundException(executionId: String) : RuntimeException("Execution: $executionId not found")
