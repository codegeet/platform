package io.codegeet.problems.submissions.model

import org.springframework.data.mongodb.repository.MongoRepository

interface SubmissionRepository : MongoRepository<Submission, String> {
    fun findByProblemId(problemId: String): List<Submission>
}