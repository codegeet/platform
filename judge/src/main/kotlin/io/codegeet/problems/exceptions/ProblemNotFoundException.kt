package io.codegeet.problems.exceptions

class ProblemNotFoundException(problemId: String) : RuntimeException("Problem with id: $problemId not found.")
