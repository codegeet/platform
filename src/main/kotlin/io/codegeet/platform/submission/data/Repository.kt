package io.codegeet.platform.submission.data

import org.springframework.data.repository.CrudRepository

interface SubmissionRepository : CrudRepository<Submission, String>
interface ExecutionsInputOutputRepository : CrudRepository<Execution, String>
