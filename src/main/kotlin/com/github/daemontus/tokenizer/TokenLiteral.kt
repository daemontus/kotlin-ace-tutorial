package com.github.daemontus.tokenizer

/**
 * Partial implementation of [TokenRule] which matches a fixed constant token.
 */
interface TokenLiteral<R: TokenRule<R>> : TokenRule<R> {

    /** [Token.value] of this token will be used for matching. Upon match, this instance is returned. **/
    val token: Token<R>

    override fun readToken(line: String, position: Int): Token<R>? = token.takeIf {
        line.startsWith(token.value, position)
    }

}