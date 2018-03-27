package com.github.daemontus.tokenizer

import com.github.daemontus.Formula
import com.github.daemontus.ModelFile
import com.github.daemontus.unescapeChar

typealias Annotations = Map<String, List<Formula>>

fun List<String>.parseLines(): ModelFile {
    val declarations = ArrayList<ModelFile.Declaration>()
    var annotations: Annotations = emptyMap()
    val tokenizer = OdeTokenizer()
    var state = emptyList<OdeTokenizer.State>()
    for (line in this) {
        val (allTokens, nextState) = tokenizer.tokenizeLine(line, state)
        state = nextState
        val tokens = allTokens.purgeIgnoredTokens()
        tokens.parseAnnotation()?.let { (annotation, _) ->
            annotations += annotation
        } ?: tokens.parseDeclaration(annotations)?.let { (declaration, _) ->
            annotations = emptyMap()
            declarations.add(declaration)
        }
    }
    return ModelFile(declarations)
}

private fun List<OdeToken>.parseAnnotation(): Pair<Pair<String, List<Formula>>, Int>? {
    return this.getOrNull(0)?.takeIf { it.rule === Rules.Identifier.Annotation }?.let {
        val (function, continueAt) = parseFunctionCall(0) ?: error("Expected annotation")
        if (continueAt < size) error("Unexpected token at $continueAt: ${this[continueAt]}")
        (function.name.drop(1) to function.args) to continueAt
    }
}

private fun List<OdeToken>.parseDeclaration(annotations: Annotations): Pair<ModelFile.Declaration, Int>? {
    return  (parseExternal(0, annotations)
            ?: parseVariable(0, annotations)
            ?: parseParameter(0, annotations)
            ?: parseFlow(0, annotations)
            ?: parseConstant(0, annotations)
            ?: parseFunction(0, annotations))?.also { (_, next) ->
        if (next < size) error("Unexpected token at $next: ${this[next]} in ${this}")
    }
}

private fun List<OdeToken>.parseFunction(at: Int, annotations: Annotations): Pair<ModelFile.Declaration.Function, Int>? {
    return this.getOrNull(at)?.takeIf { it.rule === Rules.Declaration.Function }?.let {
        val id = this.getOrNull(at + 1)?.takeIf { it.rule is Rules.Identifier } ?: error("Expected identifier.")
        val (args, continueAt) = parseArgumentDeclaration(at + 2) ?: error("Expected argument declaration")
        if (this.getOrNull(continueAt)?.rule !== Rules.Misc.Equal) error("Expected '='")
        val (formula, next) = parseFormula(continueAt + 1) ?: error("Expected formula.")
        ModelFile.Declaration.Function(id.value, args, formula, annotations) to next
    }
}

private fun List<OdeToken>.parseArgumentDeclaration(at: Int): Pair<List<String>, Int>? {
    var t = at
    val result = ArrayList<String>()
    if (t < size && this[t].rule === Rules.Misc.Parentheses.Open) {
        t += 1
        while (t < size) {
            if (this[t].rule === Rules.Misc.Parentheses.Close) break
            if (t < size && this[t].rule is Rules.Identifier) result.add(this[t].value) else error("Expected ")
            t += 1
            if (t < size && this[t].rule === Rules.Misc.Parentheses.Close) break
            if (t < size && this[t].rule === Rules.Misc.Comma) t += 1
        }
        if (t == size && this[t-1].rule === Rules.Misc.Comma) error("Unclosed function declaration")
        else t += 1
    }
    return result to t
}

private fun List<OdeToken>.parseParameter(at: Int, annotations: Annotations): Pair<ModelFile.Declaration.Parameter, Int>? {
    return this.getOrNull(at)?.takeIf { it.rule === Rules.Declaration.Parameter }?.let {
        val id = this.getOrNull(at + 1)?.takeIf { it.rule is Rules.Identifier } ?: error("Expected identifier.")
        if (this.getOrNull(at + 2)?.rule !== Rules.Misc.In) error("Expected 'in'.")
        val (range, continueAt) = parseRange(at + 3) ?: error("Expected range.")
        ModelFile.Declaration.Parameter(id.value, range, annotations) to continueAt
    }
}

private fun List<OdeToken>.parseVariable(at: Int, annotations: Annotations): Pair<ModelFile.Declaration.Variable, Int>? {
    return this.getOrNull(at)?.takeIf { it.rule === Rules.Declaration.Variable }?.let {
        val id = this.getOrNull(at + 1)?.takeIf { it.rule is Rules.Identifier } ?: error("Expected identifier.")
        if (this.getOrNull(at + 2)?.rule !== Rules.Misc.In) error("Expected 'in'.")
        val (range, continueAt) = parseRange(at + 3) ?: error("Expected range.")
        ModelFile.Declaration.Variable(id.value, range, annotations) to continueAt
    }
}

private fun List<OdeToken>.parseFlow(at: Int, annotations: Annotations): Pair<ModelFile.Declaration.Flow, Int>? {
    return this.getOrNull(at)?.takeIf { it.rule === Rules.Declaration.Flow }?.let {
        val id = this.getOrNull(at + 1)?.takeIf { it.rule is Rules.Identifier } ?: error("Expected identifier.")
        if (this.getOrNull(at + 2)?.rule !== Rules.Misc.Equal) error("Expected '='.")
        val (formula, continueAt) = parseFormula(at + 3) ?: error("Expected formula.")
        ModelFile.Declaration.Flow(id.value, formula, annotations) to continueAt
    }
}

private fun List<OdeToken>.parseConstant(at: Int, annotations: Annotations): Pair<ModelFile.Declaration.Constant, Int>? {
    return this.getOrNull(at)?.takeIf { it.rule === Rules.Declaration.Constant }?.let {
        val id = this.getOrNull(at + 1)?.takeIf { it.rule is Rules.Identifier } ?: error("Expected identifier.")
        if (this.getOrNull(at + 2)?.rule !== Rules.Misc.Equal) error("Expected '='.")
        val (formula, continueAt) = parseFormula(at + 3) ?: error("Expected formula.")
        ModelFile.Declaration.Constant(id.value, formula, annotations) to continueAt
    }
}

private fun List<OdeToken>.parseExternal(at: Int, annotations: Annotations): Pair<ModelFile.Declaration.External, Int>? {
    return if (at >= size - 1 || this[at].rule != Rules.Declaration.External || this[at + 1].rule !is Rules.Identifier) null else {
        ModelFile.Declaration.External(this[at + 1].value, annotations) to (at + 2)
    }
}

private fun List<OdeToken>.parseRange(at: Int): Pair<Pair<Formula, Formula>, Int>? {
    return if (at >= size || this[at].rule != Rules.Misc.Brackets.Open) null else {
        val (low, next) = parseFormula(at + 1) ?: error("Expected formula")
        if (next >= size || this[next].rule != Rules.Misc.Range) error("Expected '..' after range lower bound")
        val (high, end) = parseFormula(next + 1) ?: error("Expected formula")
        if (end >= size || this[end].rule != Rules.Misc.Brackets.Close) error("Expected ']' after upper bound")
        (low to high) to (end + 1)
    }
}

private fun List<OdeToken>.parseFormula(at: Int): Pair<Formula, Int>? = parseSubtraction(at)

private fun List<OdeToken>.parseSubtraction(at: Int): Pair<Formula, Int>?
    = parseInfixOperator(at, Rules.Math.Subtraction, ::parseAddition)

private fun List<OdeToken>.parseAddition(at: Int): Pair<Formula, Int>?
    = parseInfixOperator(at, Rules.Math.Addition, ::parseMultiplication)

private fun List<OdeToken>.parseMultiplication(at: Int): Pair<Formula, Int>?
    = parseInfixOperator(at, Rules.Math.Multiplication, ::parseDivision)

private fun List<OdeToken>.parseDivision(at: Int): Pair<Formula, Int>?
    = parseInfixOperator(at, Rules.Math.Division) {parseBracketsOrNested(it, ::parseUnary) }

private fun List<OdeToken>.parseUnary(at: Int): Pair<Formula, Int>? {
    return if (at >= size) null else {
        when (this[at].rule) {
            Rules.Math.Addition, Rules.Math.Subtraction -> {
                val (inner, next) = parseBracketsOrNested(at + 1, ::parseUnary) ?: error("Expected formula")
                Formula.Function(this[at].rule.id, listOf(inner)) to next
            }
            else -> parseBracketsOrNested(at, ::parseAtom)
        }
    }
}

private fun List<OdeToken>.parseInfixOperator(at: Int, op: Rules, nested: (Int) -> Pair<Formula, Int>?): Pair<Formula, Int>? {
    val (arg1, next1) = nested(at) ?: error("Expected formula")
    var next = next1
    val args = mutableListOf(arg1)
    println("Got $arg1 for $op with next ${this.getOrNull(next)}")
    while (next < size && this[next].rule == op) {
        val (argN, nextN) = nested(next + 1) ?: error("Expected formula")
        args.add(argN)
        next = nextN
    }
    return if (args.size == 1) arg1 to next else Formula.Function(op.id, args) to next
}

private fun List<OdeToken>.parseBracketsOrNested(at: Int, nested: (Int) -> Pair<Formula, Int>?): Pair<Formula, Int>? {
    return when {
        at >= size -> null
        this[at].rule !== Rules.Misc.Parentheses.Open -> nested(at)
        else -> {
            parseFormula(at+1)?.let { (formula, continueAt) ->
                if (continueAt >= size || this[continueAt].rule !== Rules.Misc.Parentheses.Close) {
                    error("Unclosed parentheses in $this")
                }
                println("Finished $formula")
                formula to continueAt + 1
            }
        }
    }
}

private fun List<OdeToken>.parseAtom(at: Int): Pair<Formula, Int>? {
    return parseNumber(at)
            ?: parseText(at)
            ?: parseFunctionCall(at)
            //?: error("Expected literal or function call, but found ${this.getOrNull(at)} in $this")
}

private fun List<OdeToken>.parseNumber(at: Int): Pair<Formula.Number, Int>? {
    return if (at >= size || this[at].rule !== Rules.NumberLiteral) null else {
        Formula.Number(this[at].value.toDouble()) to (at + 1)
    }
}

private fun List<OdeToken>.parseText(at: Int): Pair<Formula.Text, Int>? {
    return if (at >= size || this[at].rule !== Rules.StringLiteral.Open) null else {
        var t = at + 1
        val string = StringBuilder()
        while (t < size && this[t].rule !== Rules.StringLiteral.Close) {
            when {
                this[t].rule == Rules.StringLiteral.Value -> string.append(this[t].value)
                this[t].rule == Rules.StringLiteral.Escape -> string.append(this[t].value[1].unescapeChar())
                else -> error("Unexpected token ${this[t]}")
            }
            t += 1
        }
        if (t == size) error("Unclosed string literal")
        Formula.Text(string.toString()) to t + 1
    }
}

private fun List<OdeToken>.parseFunctionCall(at: Int): Pair<Formula.Function, Int>? {
    return if (at >= size || this[at].rule !is Rules.Identifier) null else {
        val name = this[at].value
        val args = ArrayList<Formula>()
        var t = at + 1
        if (t < size && this[t].rule === Rules.Misc.Parentheses.Open) {
            t += 1
            while (t < size) {
                if (this[t].rule == Rules.Misc.Parentheses.Close) break
                parseFormula(t)?.let { (arg, continueAt) ->
                    args.add(arg)
                    t = continueAt
                } ?: error("Expected formula as argument to $name")
                if (t < size) {
                    if (this[t].rule == Rules.Misc.Parentheses.Close) break
                    if (this[t].rule === Rules.Misc.Comma) t += 1
                }
            }
            if (t == size && this[t-1].rule == Rules.Misc.Comma) error("Unclosed argument list.")
            else t += 1
        }
        Formula.Function(name, args) to t
    }
}

fun List<OdeToken>.purgeIgnoredTokens(): List<OdeToken> = this.filter {
    it.rule !== Rules.Whitespace && it.rule !is Rules.Comment
}

abstract class OdeParser {



    companion object {

        fun inferTypes(allTokens: List<OdeToken>): Map<String, String> {
            val tokens = allTokens.purgeIgnoredTokens()
            val result = HashMap<String, String>()

            if (tokens.size >= 2) {
                val declaration = tokens[0]
                val name = tokens[1]
                if (name.rule is Rules.Identifier && declaration.rule is Rules.Declaration) {
                    val id = name.value
                    when (declaration.rule) {
                        Rules.Declaration.External -> result[id] = Rules.Identifier.External.id
                        Rules.Declaration.Parameter -> result[id] = Rules.Identifier.Parameter.id
                        Rules.Declaration.Variable -> result[id] = Rules.Identifier.Variable.id
                        Rules.Declaration.Constant -> result[id] = Rules.Identifier.Constant.id
                        Rules.Declaration.Function -> {
                            result[id] = Rules.Identifier.Function.id
                            // read local variable declarations
                            if (tokens.getOrNull(2)?.rule == Rules.Misc.Parentheses.Open) {
                                var i = 3
                                while (i < tokens.size && tokens[i].rule != Rules.Misc.Parentheses.Close) {
                                    if (tokens[i] is Rules.Identifier) {
                                        result[tokens[i].value] = Rules.Identifier.Local.id
                                    }
                                    i += 1 // +1 for identifier
                                }
                            }
                        }
                    }
                }
            }

            return result
        }

    }

}