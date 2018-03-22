package com.github.daemontus.theme

fun Theme.compileCSS(): String = styleRules.map { (selectors, styles) ->
    val styleRules = styles.map { (key, style) ->
        "    $key: $style;"
    }.joinToString(separator = "\n")
    val selectorString = selectors.joinToString(separator = ", \n") { ".$name $it" }
    "$selectorString { \n$styleRules\n }"
}.joinToString(separator = "\n")

fun style(
        color: String? = null, background: String? = null, fontStyle: String? = null, textDecoration: String? = null, fontWeight: String? = null
): Map<String, String> {
    val map = HashMap<String, String>(4)
    if (color != null) map["color"] = color
    if (background != null) map["background"] = background
    if (fontStyle != null) map["font-style"] = fontStyle
    if (textDecoration != null) map["text-decoration"] = textDecoration
    if (fontWeight != null) map["font-weight"] = fontWeight
    return map
}