package com.github.daemontus

sealed class Formula {

    data class Number(val value: Double) : Formula() {
        override fun toString(): String = value.toString()
    }

    data class Text(val value: String) : Formula() {
        override fun toString(): String = "\"$value\""
    }

    data class Function(val name: String, val args: List<Formula>) : Formula() {
        override fun toString(): String = "$name(${args.joinToString(separator = ", ")})"
    }

}