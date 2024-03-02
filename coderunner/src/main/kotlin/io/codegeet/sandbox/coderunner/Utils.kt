package io.codegeet.sandbox.coderunner

import java.io.InputStream

fun InputStream.readAsText(limit: Int = 2_000) = this.bufferedReader(Charsets.UTF_8)
    .use { reader ->
        with(CharArray(limit)) {
            reader.read(this, 0, limit).takeIf { it > 0 }?.let { String(this, 0, it) } ?: ""
        }
    }

fun String?.takeIfNotEmpty(): String? = this?.takeIf { it.isNotEmpty() }
