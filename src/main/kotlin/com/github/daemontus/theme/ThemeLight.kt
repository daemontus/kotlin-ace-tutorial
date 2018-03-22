package com.github.daemontus.theme

import com.github.daemontus.tokenizer.Rules
import com.github.daemontus.tokenizer.Rules.*
import com.github.daemontus.tokenizer.aceTypeToSelector

/**
 * Light theme based on default JetBrains editor theme.
 */
object ThemeLight : Theme {

    override val isDark: Boolean = false
    override val name: String = "idea-light"

    override val styleRules: Map<List<String>, Map<String, String>>
        get() = mapOf(
                // general set of rules for editor styling
                listOf("") to style(color = Colors.textBlack, background = Colors.bg300),
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
                        Misc.In.toSelector()
                ) to style(color = Colors.keywordBlue),

                listOf(
                        Declaration.ID_PREFIX.aceTypeToSelector()
                ) to style(fontWeight = "bold"),

                listOf(Unknown.toSelector()) to style(textDecoration = "red underline"),

                listOf(Identifier.External.toSelector()) to style(color = Colors.globalViolet, fontStyle = "italic"),

                listOf(Identifier.Constant.toSelector()) to style(color = Colors.localYellow, fontStyle = "italic"),

                listOf(Identifier.Variable.toSelector()) to style(color = Colors.localYellow),

                listOf(Identifier.Parameter.toSelector()) to style(color = Colors.parameterTeal),

                listOf(Identifier.Function.toSelector()) to style(color = Colors.globalViolet)

        )

    private fun Rules.toSelector() = this.id.aceTypeToSelector()

    object Colors {
        const val keywordBlue = "#000080"
        const val stringGreen = "#008000"
        const val numberBlue = "#0000FF"
        const val globalViolet = "#660E7A"
        const val localYellow = "#9876AA"
        const val parameterTeal = "#20999D"
        const val textBlack = "#000000"

        @Suppress("unused") // used in css
        const val bg700 = "#ECECEC"
        const val bg500 = "#F0F0F0"
        const val bg300 = "#FFFFFF"

        @Suppress("unused") // used in css
        const val separator = "#AEAEAE"
    }
}