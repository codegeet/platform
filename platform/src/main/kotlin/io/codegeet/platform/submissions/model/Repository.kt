package io.codegeet.platform.submissions.model

import org.springframework.data.repository.CrudRepository

interface SubmissionRepository : CrudRepository<Submission, String>
