package com.github.daemontus.tokenizer

/**
 * Token rule is a simple class that can match a specific type of token.
 *
 * It is typically part of a larger rule set [R].
 */
interface TokenRule<R: TokenRule<R>> {

    /**
     * Try to make a token based on this rule in [line] at [position].
     * If rule does not match, return null.
     */
    fun readToken(line: String, position: Int): Token<R>?

}