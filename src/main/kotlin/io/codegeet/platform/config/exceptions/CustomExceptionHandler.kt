package io.codegeet.platform.config.exceptions

import io.codegeet.platform.submission.exceptions.SubmissionNotFoundException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

@ControllerAdvice
class CustomExceptionHandler {

    @ExceptionHandler(SubmissionNotFoundException::class)
    fun handleExecutionNotFoundException(e: SubmissionNotFoundException): ResponseEntity<String> {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.message)
    }
}
