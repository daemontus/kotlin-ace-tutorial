package com.github.daemontus.tokenizer

import com.github.daemontus.tokenizer.Rules.*

open class OdeTokenizer {

    sealed class State {
        object String : State()
        object LineComment : State()
        object BlockComment : State()
    }

    var globalTypes: Map<String, Identifier> = emptyMap()

    fun tokenizeLine(line: String, startState: List<State>): Pair<List<OdeToken>, List<State>> {
        val tokens = ArrayList<OdeToken>()
        var state = startState
        var position = 0
        do {
            val (token, newState) = readToken(line, position, state, emptyMap())
            state = newState
            if (token != null) {
                tokens.add(token)
                position += token.value.length
            }
        } while (token != null)

        return tokens to state
    }

    private val mainTokens = listOf(
            Comment.Block.Open, Comment.Block.Close,                                            // /* */
            Comment.Line.StartPython, Comment.Line.StartC,                                      // # //
            Math.Addition, Math.Subtraction, Math.Multiplication, Math.Division,                // + - * /
            Misc.Comma, Misc.Range, Misc.Equal,                                                 // , .. =
            Misc.Parentheses.Open, Misc.Parentheses.Close,                                      // ( )
            Misc.Brackets.Open, Misc.Brackets.Close,                                            // [ ]
            StringLiteral.Open, Whitespace,                                                     // " \s
            NumberLiteral, Identifier.Undefined,
            Unknown
    )

    private val keywords: List<TokenLiteral<Rules>> = listOf(
            Misc.In, Declaration.Variable, Declaration.Parameter, Declaration.Constant,
            Declaration.Function, Declaration.External, Declaration.Flow
    )

    private fun readToken(line: String, position: Int, state: List<State>, localTypes: Map<String, Identifier>): Pair<OdeToken?, List<State>> {
        return when (state.lastOrNull()) {
            State.String -> {
                val token = readStringToken(line, position)
                val nextState = if (token == null || token.rule == StringLiteral.Close) state.dropLast(1) else state
                token to nextState
            }
            State.BlockComment -> {
                val token = readBlockCommentToken(line, position)
                val nextState = when (token?.rule) {
                    Comment.Block.Open -> state + State.BlockComment
                    Comment.Block.Close -> state.dropLast(1)
                    else -> state
                }
                token to nextState
            }
            State.LineComment -> {
                Comment.Line.Text.readToken(line, position) to state.dropLast(1)
            }
            null -> {
                val rawToken = readTokenFromRules(line, position, mainTokens)

                val token: OdeToken? = when (rawToken?.rule) {
                    Identifier.Undefined -> {
                        matchExactFromRules(rawToken, keywords) ?: run {
                            // If token is not a keyword, we have to give it a type!
                            val value = rawToken.value
                            if (value.startsWith('@')) {
                                Token(Identifier.Annotation, value)
                            } else {
                                val type = localTypes[value] ?: globalTypes[value]
                                if (type != null) {
                                    Token(type, value)
                                } else {
                                    rawToken
                                }
                            }
                        }
                    }
                    else -> rawToken
                }

                val nextState = when (token?.rule) {
                    Comment.Block.Open -> state + State.BlockComment
                    StringLiteral.Open -> state + State.String
                    Comment.Line.StartC, Comment.Line.StartPython -> state + State.LineComment
                    else -> state
                }

                token to nextState
            }
        }
    }

    /**
     * Try to exactly match one of the given token literals.
     */
    private fun matchExactFromRules(rawToken: OdeToken, rules: List<TokenLiteral<Rules>>): OdeToken? =
            rules.asSequence().map {
                it.token.takeIf { it.value == rawToken.value }
            }.filterNotNull().firstOrNull()

    /**
     * Go through the given rules in the specified order nad try to match the tokens.
     */
    private fun readTokenFromRules(line: String, position: Int, rules: List<Rules>): OdeToken? =
            rules.asSequence().map {
                it.readToken(line, position)
            }.filterNotNull().firstOrNull()

    /**
     * Tries to parse next block comment token from [line] at [position].
     *
     * Assumes first Block.Open has already been parsed!!
     */
    private fun readBlockCommentToken(line: String, position: Int): OdeToken? =
            Comment.Block.Close.readToken(line, position)
            ?: Comment.Block.Open.readToken(line, position)
            ?: Comment.Block.Text.readToken(line, position)

    /**
     * Tries to parse next string token from [line] at [position].
     *
     * Assumes the first string token has been already parsed!!
     **/
    private fun readStringToken(line: String, position: Int): OdeToken? =
            StringLiteral.Close.readToken(line, position)
            ?: StringLiteral.Escape.readToken(line, position)
            ?: StringLiteral.Value.readToken(line, position)

}