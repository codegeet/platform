package io.codegeet.problems.executions.exceptions

class SolutionNotFoundException(problemId: String) : RuntimeException("Solution for problem: '$problemId' not found")
