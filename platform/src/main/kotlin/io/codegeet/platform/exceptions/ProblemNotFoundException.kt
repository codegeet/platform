package io.codegeet.platform.exceptions

class ProblemNotFoundException(problemId: String) : RuntimeException("Problem with id: $problemId not found.")
