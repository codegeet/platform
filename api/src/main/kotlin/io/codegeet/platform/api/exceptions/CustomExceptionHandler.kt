package io.codegeet.platform.api.exceptions

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(HttpStatus.NOT_FOUND)
class ExecutionNotFoundException(message: String? = null) : RuntimeException(message)