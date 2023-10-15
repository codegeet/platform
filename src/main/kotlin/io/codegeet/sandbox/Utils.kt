package io.codegeet.sandbox

import java.io.InputStream

fun InputStream.readAsText() = this.bufferedReader(Charsets.UTF_8).use { it.readText() }