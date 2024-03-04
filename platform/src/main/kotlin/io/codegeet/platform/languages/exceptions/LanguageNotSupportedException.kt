package io.codegeet.judge.languages.exceptions

import io.codegeet.common.Language

class LanguageNotSupportedException(language: Language) : RuntimeException("Language '$language' is not supported.")
