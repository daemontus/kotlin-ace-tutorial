package ace

/**
 * https://ace.c9.io/#nav=api&api=editor
 */
external interface Editor {

    fun setTheme(themePath: String)

    fun getSession(): EditSession

}