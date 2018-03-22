package com.github.daemontus.tokenizer

// number is always positive - negative numbers are applications of unary minus
val NUMBER_LITERAL_REGEX = Regex("\\d+(?:\\.\\d+)?(?:e-?\\d+)?")
val IDENTIFIER_REGEX = Regex("@?[a-zA-Z][a-zA-Z0-9_:]*")

/**
 * Convert Ace token type to the corresponding CSS selector.
 *
 * foo.goo -> .ace_foo.ace_goo
 */
fun String.aceTypeToSelector() = this.splitToSequence(".").map {
    ".ace_$it"
}.joinToString(separator = "")

/**
 * Try to match regex starting at [position]. If unsuccessful, return null.
 */
fun matchRegex(line: String, position: Int, regex: Regex): String? {
    val match = regex.find(line, position)
    return if (match == null || match.range.first != position) null else {
        line.substring(match.range)
    }
}

/**
 * Return a substring (or null if empty) of characters matching [condition] in [this] starting at [position].
 */
inline fun String.scanWhile(position: Int, condition: String.(Int) -> Boolean): String? {
    var i = position
    while (i < length && condition(i)) i += 1
    return if (i == position) null else substring(position until i)
}