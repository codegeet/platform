package io.codegeet.problems.executions.model

import org.springframework.data.repository.CrudRepository

interface SubmissionRepository : CrudRepository<Submission, String> {
    fun findByProblemId(problemId: String): List<Submission>
}
