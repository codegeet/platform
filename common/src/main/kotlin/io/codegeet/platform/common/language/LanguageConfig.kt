package io.codegeet.platform.common.language

object LanguageConfig {

    private val config = mapOf(
        Language.JAVA to Config(
            compilation = "javac Main.java",
            invocation = "java Main",
            fileName = "Main.java"
        ),
        Language.PYTHON to Config(
            compilation = null,
            invocation = "python3 app.py",
            fileName = "app.py"
        ),
        Language.CSHARP to Config(
            compilation = "mcs -out:app.exe app.cs",
            invocation = "mono app.exe",
            fileName = "app.cs"
        ),
        Language.JS to Config(
            compilation = null,
            invocation = "node app.js",
            fileName = "app.js"
        ),
        Language.TS to Config(
            compilation = "tsc app.ts",
            invocation = "node app.js",
            fileName = "app.ts"
        ),
        Language.KOTLIN to Config(
            compilation = "kotlinc app.kt -include-runtime -d app.jar",
            invocation = "java -jar app.jar",
            fileName = "app.kt"
        )
    )

    fun get(language: Language): Config =
        config[language] ?: throw IllegalArgumentException("Config for $language not found")

    data class Config(
        val compilation: String?,
        val invocation: String,
        val fileName: String,
    )
}
