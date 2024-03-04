package io.codegeet.platform.problems.data

import io.codegeet.common.Language
import jakarta.persistence.*

@Entity
@Table(name = "problems")
data class Problem(
    @Id
    val problemId: String,
    val name: String,
    val number: Int,
    val description: String,

    @OneToMany(cascade = [CascadeType.ALL])
    val snippets: MutableList<ProblemSnippet> = mutableListOf(),

    @OneToMany(cascade = [CascadeType.ALL])
    val cases: MutableList<ProblemCase> = mutableListOf(),

    @OneToOne(cascade = [CascadeType.ALL])
    val solution: ProblemSolution? = null
)

@Entity
@Table(name = "problem_snippets")
data class ProblemSnippet(
    @Id
    val snippetId: String,
    @Enumerated(EnumType.STRING)
    val language: Language,

    @Column(columnDefinition = "TEXT")
    val snippet: String,
    val call: String,

    val problemId: String
)

@Entity
@Table(name = "problem_cases")
data class ProblemCase(
    @Id
    val caseId: String,
    val input: String,

    val problemId: String
)

@Entity
@Table(name = "problem_solutions")
data class ProblemSolution(
    @Id
    val solutionId: String,
    val problemId: String,
    @Enumerated(EnumType.STRING)
    val language: Language,
    @Column(columnDefinition = "TEXT")
    val snippet: String,
)
