package io.codegeet.problems.problems

import io.codegeet.problems.problems.model.Problem
import io.codegeet.problems.problems.model.ProblemsRepository
import io.codegeet.problems.exceptions.ProblemNotFoundException
import org.springframework.stereotype.Service
import kotlin.jvm.optionals.getOrElse

@Service
class ProblemService(private val problemRepository: ProblemsRepository) {

    fun getAll(): List<Problem> = problemRepository.findAll().toList()

    fun get(problemId: String): Problem =
        problemRepository.findById(problemId).getOrElse { throw ProblemNotFoundException(problemId) }
}
