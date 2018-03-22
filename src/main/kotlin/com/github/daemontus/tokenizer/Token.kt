package com.github.daemontus.tokenizer

/**
 * Token represents an atomic unit of text. It carries information about which [rule]
 * was used to match it, and the corresponding matched [value].
 *
 * It is parametrised by a rule-set [R] which you can use to filter and classify tokens.
 */
data class Token<R: TokenRule<R>>(
        val rule: R, override val value: String
) : AceToken {

    override val type: String = rule.toString()

}