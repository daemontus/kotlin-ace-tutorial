package com.github.daemontus

import com.github.daemontus.tokenizer.Rules
import kotlin.math.pow
import kotlin.math.sin

fun ModelFile.validate(): OdeModel {
    val variables = this.declarations.filterIsInstance<ModelFile.Declaration.Variable>()
    val flows = this.declarations.filterIsInstance<ModelFile.Declaration.Flow>()
    val nonFlows = this.declarations.filter { it !is ModelFile.Declaration.Flow }
    val vars = variables.map { v ->
        val flow = flows.find { it.name == v.name } ?: error("No flow for variable ${v.name}")
        OdeModel.Variable(
                name = v.name,
                bounds = execute(v.bounds.first, nonFlows) to execute(v.bounds.second, nonFlows),
                flow = simplify(resolve(flow.value, nonFlows))
        )
    }
    return OdeModel(vars)
}

fun execute(formula: Formula, declarations: List<ModelFile.Declaration>, locals: Map<String, Double> = emptyMap()): Double {
    return when (formula) {
        is Formula.Text -> error("Can't evaluate string ${formula.value}")
        is Formula.Number -> formula.value
        is Formula.Function -> {
            val args = formula.args.map { execute(it, declarations, locals) }
            when (formula.name) {
                Rules.Math.Addition.id -> args.fold(0.0) { a, b -> a + b }
                Rules.Math.Subtraction.id -> if (args.size == 1) {
                    -1 * args[0]
                } else {
                    args.drop(1).fold(args[0]) { a, b -> a - b }
                }
                Rules.Math.Multiplication.id -> args.fold(1.0) { a, b -> a * b }
                Rules.Math.Division.id -> {
                    if (args.size < 2) error("Division with less than two arguments!")
                    args.drop(1).fold(args[0]) { a, b -> a / b }
                }
                "pow" -> if (args.size != 2) error("Pow has ${args.size} arguments instead of two!") else {
                    args[0].pow(args[1])
                }
                "sin" -> if (args.size != 1) error("Sin has ${args.size} arguments instead of one!") else {
                    sin(args[1])
                }
                in locals -> locals[formula.name]!!
                else -> {
                    val dec = declarations.find { it.name == formula.name } ?: error("Unknown reference ${formula.name}")
                    when (dec) {
                        is ModelFile.Declaration.Constant -> {
                            if (args.isNotEmpty()) error("Constant has no arguments!")
                            execute(dec.value, declarations)
                        }
                        is ModelFile.Declaration.Function -> {
                            if (args.size != dec.arguments.size) error("Function arity does not match!")
                            val inner = dec.arguments.mapIndexed { i, name -> name to args[i] }.toMap()
                            execute(dec.value, declarations, inner)
                        }
                        else -> error("Reference to $dec in constant expression.")
                    }
                }
            }
        }
    }
}

fun resolve(formula: Formula, declarations: List<ModelFile.Declaration>, locals: Map<String, Formula> = emptyMap()): Formula {
    return when (formula) {
        is Formula.Text -> formula
        is Formula.Number -> formula
        is Formula.Function -> {
            val args = formula.args.map { resolve(it, declarations, locals) }
            when (formula.name) {
                in locals -> locals[formula.name]!!
                Rules.Math.Multiplication.id -> Formula.Function("*", args)
                Rules.Math.Addition.id -> Formula.Function("+", args)
                Rules.Math.Division.id -> Formula.Function("/", args)
                Rules.Math.Subtraction.id -> Formula.Function("-", args)
                "pow", "sin" -> Formula.Function(formula.name, args)
                else -> {
                    val dec = declarations.find { it.name == formula.name } ?: error("Unknown reference ${formula.name}")
                    when (dec) {
                        is ModelFile.Declaration.Constant -> Formula.Number(execute(dec.value, declarations))
                        is ModelFile.Declaration.Variable -> if (args.isNotEmpty()) error("Variables can't have arguments") else formula
                        is ModelFile.Declaration.Function -> {
                            if (args.size != dec.arguments.size) error("Function arity does not match!")
                            val inner = dec.arguments.mapIndexed { i, name -> name to args[i] }.toMap()
                            resolve(dec.value, declarations, inner)
                        }
                        else -> error("Reference to $dec is not allowed here.")
                    }
                }
            }
        }
    }
}

fun simplify(formula: Formula): Formula {
    return when (formula) {
        is Formula.Function -> {
            val args = formula.args.map { simplify(it) }
            when (formula.name) {
                "*" -> {
                    val constants = args.filterIsInstance<Formula.Number>()
                    val other = args - constants
                    val folded = constants.fold(1.0) { a, b -> a * b.value }
                    val newArgs = if (folded == 1.0) other else other + Formula.Number(folded)
                    if (newArgs.size == 1) newArgs[0] else {
                        Formula.Function(formula.name, newArgs)
                    }
                }
                "+" -> {
                    val constants = args.filterIsInstance<Formula.Number>()
                    val other = args - constants
                    val folded = constants.fold(0.0) { a, b -> a + b.value }
                    val newArgs = if (folded == 0.0) other else other + Formula.Number(folded)
                    if (newArgs.size == 1) newArgs[0] else {
                        Formula.Function(formula.name, newArgs)
                    }
                }
                else -> Formula.Function(formula.name, args)
            }
        }
        else -> formula
    }
}