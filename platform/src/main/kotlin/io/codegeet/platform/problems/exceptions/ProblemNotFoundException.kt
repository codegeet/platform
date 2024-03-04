package io.codegeet.platform.problems.exceptions

class ProblemNotFoundException(problemId: String) : RuntimeException("Problem with id: $problemId not found.")
