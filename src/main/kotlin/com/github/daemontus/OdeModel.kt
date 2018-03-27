package com.github.daemontus

data class OdeModel(
        val variables: List<Variable>
) {

    data class Variable(
            val name: String,
            val bounds: Pair<Double, Double>,
            val flow: Formula
    ) {

        override fun toString(): String {
            return """
                var $name in [${bounds.first}..${bounds.second}]
                flow $name = $flow
            """.trimIndent()
        }
    }

    override fun toString(): String = variables.joinToString(separator = "\n")

}