package ace

import com.github.daemontus.tokenizer.AceToken

external interface Ace {

    /**
     * Define a new ace.Ace module at given [path] using a given [init] function.
     */
    fun define(path: String, dependencies: Array<String>, init: (Require, dynamic) -> dynamic)

    /**
     * Create a new editor in the element with given [id].
     */
    fun edit(id: String): Editor

    fun <T> require(moduleName: String): T

    /**
     * Undocumented.
     * Responsible for implementing inheritance for non-Kotlin JS classes.
     */
    interface OOP {
        fun inherits(child: JsClass<*>, parent: JsClass<*>)
    }

    /**
     * Undocumented.
     * Provides description of a single language mode.
     */
    interface Mode<Token: AceToken, State: Any?> {

        /**
         * WARNING: You have to assign tokenizer AFTER constructor is finished,
         * because you have to hook up your mode to it's super-mode via ace.OOP,
         * but this will override kotlin constructor!
         */
        @JsName("\$tokenizer")
        var tokenizer: Tokenizer<Token, State>?

    }

    /**
     * Undocumented.
     * Module which provides access to the root text mode javascript class.
     */
    interface TextModeModule {
        @JsName("Mode")
        val mode: JsClass<Mode<*, *>>
    }


}

