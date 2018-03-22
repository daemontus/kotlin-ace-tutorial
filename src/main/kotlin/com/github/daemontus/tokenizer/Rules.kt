package com.github.daemontus.tokenizer

/**
 * Describes the rules for matching our tokens.
 */
sealed class Rules(val id: String) : TokenRule<Rules> {

    override fun toString(): String = id

    object NumberLiteral : Rules("number") {
        override fun readToken(line: String, position: Int): Token<Rules>? =
                matchRegex(line, position, NUMBER_LITERAL_REGEX)?.toToken()
    }

    object Whitespace : Rules("whitespace") {
        override fun readToken(line: String, position: Int): Token<Rules>? =
                line.scanWhile(position) { get(it).isWhitespace() }?.toToken()
    }

    // always matches a single character because we have no idea when to stop
    object Unknown : Rules("unknown") {
        override fun readToken(line: String, position: Int): Token<Rules>? =
                position.takeIf { it < line.length }?.let { line[it].toString().toToken() }
    }

    sealed class StringLiteral(id: String) : Rules("string.$id") {

        object Open : StringLiteral("open"), TokenLiteral<Rules> {
            override val token = "\"".toToken()
        }

        object Close : StringLiteral("close"), TokenLiteral<Rules> {
            override val token = "\"".toToken()
        }

        object Escape : StringLiteral("escape") {
            override fun readToken(line: String, position: Int): Token<Rules>? {
                // we need to be at least two characters before the end of the line and the first needs to be \
                return if (position >= line.length - 1 || line[position] != '\\') null else {
                    line.substring(position..(position+1))?.toToken()
                }
            }
        }

        object Value : StringLiteral("value") {
            override fun readToken(line: String, position: Int): Token<Rules>? =
                    line.scanWhile(position) { get(it) != '"' && get(it) != '\\' }?.toToken()
        }

    }

    sealed class Comment(id: String) : Rules("comment.$id") {

        sealed class Block(id: String) : Rules("block.$id") {

            object Open : Block("open"), TokenLiteral<Rules> {
                override val token = "/*".toToken()
            }

            object Close : Block("close"), TokenLiteral<Rules> {
                override val token = "*/".toToken()
            }

            object Text : Block("text") {
                override fun readToken(line: String, position: Int): Token<Rules>? =
                        line.scanWhile(position) {
                            Open.readToken(line, it) == null && Close.readToken(line, it) == null
                        }?.toToken()
            }

        }

        sealed class Line(id: String) : Rules("line.$id") {

            object StartC : Line("start.c-like"), TokenLiteral<Rules> {
                override val token = "//".toToken()
            }

            object StartPython : Line("start.python-like"), TokenLiteral<Rules> {
                override val token = "#".toToken()
            }

            object Text : Line("text") {
                override fun readToken(line: String, position: Int): Token<Rules>? =
                        line.substring(position).takeIf { it.isNotEmpty() }?.toToken()
            }

        }

    }

    sealed class Identifier(id: String) : Rules("identifier.$id") {

        // id type is decided based on external information,
        // hence all identifiers are matched identically.
        override fun readToken(line: String, position: Int): Token<Rules>? =
                matchRegex(line, position, IDENTIFIER_REGEX)?.toToken()

        object Undefined : Identifier("undefined")
        object Variable : Identifier("variable")
        object Parameter : Identifier("parameter")
        object Constant : Identifier("constant")
        object Function : Identifier("function")
        object Annotation : Identifier("annotation")
        object Local : Identifier("local")
        object External : Identifier("external")
    }

    sealed class Declaration(id: String, literal: String) : Rules("declaration.$id"), TokenLiteral<Rules> {
        override val token: Token<Rules> = literal.toToken()

        object Variable : Declaration("variable", "var")
        object Parameter : Declaration("parameter", "param")
        object Constant : Declaration("constant", "const")
        object Function : Declaration("function", "fun")
        object External : Declaration("external", "external")
        object Flow : Declaration("flow", "flow")

        companion object {
            val ID_PREFIX = "declaration"
        }
    }

    sealed class Math(id: String, literal: String) : Rules("math.$id"), TokenLiteral<Rules> {
        override val token: Token<Rules> = literal.toToken()

        object Addition : Math("addition", "+")
        object Subtraction : Math("subtraction", "-")
        object Multiplication : Math("multiplication", "*")
        object Division : Math("division", "/")
    }

    sealed class Misc(id: String, literal: String) : Rules(id), TokenLiteral<Rules> {
        override val token: Token<Rules> = literal.toToken()

        object Range : Misc("range", "")
        object Comma : Misc("comma", ",")
        object Equal : Misc("equal", "=")
        object In : Misc("in", "in")

        sealed class Parentheses(id: String, literal: String) : Misc("parentheses.$id", literal) {
            object Open : Parentheses("open", "(")
            object Close : Parentheses("close", ")")
        }

        sealed class Brackets(id: String, literal: String) : Misc("brackets.$id", literal) {
            object Open : Brackets("open", "[")
            object Close : Brackets("close", "]")
        }
    }

    protected fun String.toToken(): Token<Rules> = Token(this@Rules, this)

}