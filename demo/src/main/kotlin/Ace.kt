
typealias Require = (String) -> dynamic

external interface Ace {

    /**
     * Define a new Ace module at given [path] using a given [init] function.
     */
    fun define(path: String, dependencies: Array<String>, init: (Require, dynamic) -> dynamic)

    /**
     * Create a new editor in the element with given [id].
     */
    fun edit(id: String): Editor
}

external interface Editor {

    fun setTheme(themePath: String)

    fun getSession(): EditSession

}

external interface EditSession {

    fun getDocument(): Document

    fun setMode(modePath: String)

}

external interface Document {

    fun getValue(): String
    fun setValue(value: String)

}