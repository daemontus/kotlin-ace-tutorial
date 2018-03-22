package com.github.daemontus.theme

/**
 * Themes are objects which describe the formatting of tokens in the editor.
 *
 * Due to a lack of proper higher-level abstraction, we simple use a map of CSS selectors
 * and properties as our rule set. Eventually, this should be a little more specific,
 * but for now this is good enough.
 */
interface Theme {

    /** True if theme is dark **/
    val isDark: Boolean

    /** Theme name. This will be used as CSS class prefix for all selectors, so choose something simple but unique. **/
    val name: String

    /**
     * Style rules are a very general way of storing CSS (or other) styling rules.
     * list of CSS selectors -> (key, style)
     */
    val styleRules: Map<List<String>, Map<String, String>>

}