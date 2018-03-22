package com.github.daemontus.tokenizer

/**
 * AceToken is an interface expected by the Ace editor.
 *  - CSS classes of the token are constructed from [type].
 *  - [value] exactly matches what will be displayed in place of the token.
 */
interface AceToken {
    val type: String
    val value: String
}