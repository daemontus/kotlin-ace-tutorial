package com.github.daemontus.theme

import com.github.daemontus.tokenizer.Rules
import com.github.daemontus.tokenizer.Rules.*
import com.github.daemontus.tokenizer.aceTypeToSelector

/**
 * Dark theme based on JetBrains Darcula theme.
 */
object ThemeDark : Theme {

    override val isDark: Boolean = true
    override val name: String = "idea-dark"

    override val styleRules: Map<List<String>, Map<String, String>>
        get() = mapOf(
                // general set of rules for editor styling
                listOf("") to style(color = Colors.textGray, background = Colors.bg300),
                listOf(".ace_gutter") to style(color = "#606366", background = Colors.bg500),
                listOf(".ace_cursor") to style(color = "#ababab"),
                listOf(".ace_marker-layer .ace_selection") to style(background = "rgba(221, 240, 255, 0.20)"),
                listOf(".ace_marker-layer .ace_active-line") to style(background = "rgba(255, 255, 255, 0.031)"),
                listOf(".ace_gutter-active-line") to style(background = "rgba(255, 255, 255, 0.031)"),

                // additional rules for specific tokens

                listOf(NumberLiteral.toSelector()) to style(color = Colors.numberBlue),

                listOf(
                        StringLiteral.Open.toSelector(),
                        StringLiteral.Close.toSelector(),
                        StringLiteral.Value.toSelector()
                ) to style(color = Colors.stringGreen),

                listOf(StringLiteral.Escape.toSelector()) to style(color = "#CC7832"),

                listOf(
                        Identifier.Annotation.toSelector(),
                        Declaration.ID_PREFIX.aceTypeToSelector(),
                        Misc.Range.toSelector(),
                        Misc.Comma.toSelector(),
                        Misc.In.toSelector()
                ) to style(color = Colors.keywordOrange),

                listOf(Unknown.toSelector()) to style(textDecoration = "red underline"),

                listOf(Identifier.External.toSelector()) to style(color = Colors.globalViolet, fontStyle = "italic"),

                listOf(Identifier.Constant.toSelector()) to style(color = Colors.localYellow, fontStyle = "italic"),

                listOf(Identifier.Variable.toSelector()) to style(color = Colors.localYellow),

                listOf(Identifier.Parameter.toSelector()) to style(color = Colors.parameterTeal),

                listOf(Identifier.Function.toSelector()) to style(color = Colors.globalViolet)

        )

    private fun Rules.toSelector() = this.id.aceTypeToSelector()

    object Colors {
        const val keywordOrange = "#CC7832"
        const val stringGreen = "#6A8759"
        const val numberBlue = "#6897BB"
        const val globalViolet = "#FFC66D"
        const val localYellow = "#9876AA"
        const val parameterTeal = "#20999D"
        const val textGray = "#A9B7C6"

        @Suppress("unused") // used in css
        const val bg700 = "#3C3F41"
        const val bg500 = "#313335"
        const val bg300 = "#2B2B2B"

        @Suppress("unused") // used in css
        const val separator = "#4B4B4B"
    }
}