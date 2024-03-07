package io.codegeet.platform.problems

import io.codegeet.platform.problems.data.Problem
import io.codegeet.platform.problems.data.ProblemsRepository
import io.codegeet.platform.exceptions.ProblemNotFoundException
import org.springframework.stereotype.Service
import kotlin.jvm.optionals.getOrElse

@Service
class ProblemService(private val problemRepository: ProblemsRepository) {

    fun getAll(): List<Problem> = problemRepository.findAll().toList()

    fun get(problemId: String): Problem =
        problemRepository.findById(problemId).getOrElse { throw ProblemNotFoundException(problemId) }
}
