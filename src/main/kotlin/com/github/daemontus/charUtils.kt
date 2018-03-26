package com.github.daemontus

fun Char.escapeChar(): String = when (this) {
    '\t' -> "\\t"
    '\b' -> "\\b"
    '\n' -> "\\n"
    '\r' -> "\\r"
    '\'' -> "\\'"
    '"' -> "\\\""
    '\\' -> "\\\\"
    else -> this.toString()
}

/** Target character must be preceded with \ **/
fun Char.unescapeChar(): Char = when (this) {
    't' -> '\t'
    'b' -> '\b'
    'n' -> '\n'
    'r' -> '\r'
    else -> this
}