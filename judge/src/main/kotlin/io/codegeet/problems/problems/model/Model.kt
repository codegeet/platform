package io.codegeet.problems.problems.model

import io.codegeet.common.Language
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "problems")
data class Problem(
    @Id
    val problemId: String,
    val name: String,
    val number: Int,
    val description: String,
    val difficulty: ProblemDifficulty,
    val snippets: MutableList<ProblemSnippet> = mutableListOf(),
    val cases: MutableList<ProblemCase> = mutableListOf(),
    val solution: ProblemSolution,
    val metadata: Metadata,
)

data class ProblemSnippet(
    val language: Language,
    val snippet: String,
    val call: String,
)

data class ProblemCase(
    val input: String,
    val expected: String,
)

data class ProblemSolution(
    val problemId: String,
    val language: Language,
    val snippet: String,
)

data class Metadata(
    val params: List<MetadataParam>,
)

data class MetadataParam(
    val name: String,
)

enum class ProblemDifficulty {
    EASY, MEDIUM, HARD
}
