package io.codegeet.platform.submissions.exceptions

class SolutionNotFoundException(problemId: String) : RuntimeException("Solution for problem: '$problemId' not found")
