package com.github.daemontus

data class ModelFile(
    val declarations: List<Declaration>
) {

    sealed class Declaration {

        abstract val annotations: Map<String, List<Formula>>
        abstract val name: String

        data class Constant(
                override val name: String,
                val value: Formula,
                override val annotations: Map<String, List<Formula>>
        ) : Declaration() {
            override fun toString(): String = "const $name = $value\n"
        }

        data class Function(
                override val name: String,
                val arguments: List<String>,
                val value: Formula,
                override val annotations: Map<String, List<Formula>>
        ) : Declaration() {
            override fun toString(): String = "fun $name(${arguments.joinToString(separator = ",")}) = $value\n"
        }

        data class External(
                override val name: String,
                override val annotations: Map<String, List<Formula>>
        ) : Declaration() {
            override fun toString(): String = "external $name\n"
        }

        data class Variable(
                override val name: String,
                val bounds: Pair<Formula, Formula>,
                override val annotations: Map<String, List<Formula>>
        ) : Declaration() {
            override fun toString(): String = "var $name in [${bounds.first}..${bounds.second}]\n"
        }

        data class Parameter(
                override val name: String,
                val bounds: Pair<Formula, Formula>,
                override val annotations: Map<String, List<Formula>>
        ) : Declaration() {
            override fun toString(): String = "param $name in [${bounds.first}..${bounds.second}]\n"
        }

        data class Flow(
                override val name: String,
                val value: Formula,
                override val annotations: Map<String, List<Formula>>
        ) : Declaration() {
            override fun toString(): String = "flow $name = $value\n"
        }

    }

}