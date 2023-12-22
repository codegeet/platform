package io.codegeet.sandbox

import io.codegeet.sandbox.execution.exceptions.ExecutionNotFoundException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

@ControllerAdvice
class CustomExceptionHandler {

    @ExceptionHandler(ExecutionNotFoundException::class)
    fun handleExecutionNotFoundException(e: ExecutionNotFoundException): ResponseEntity<String> {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.message)
    }
}
