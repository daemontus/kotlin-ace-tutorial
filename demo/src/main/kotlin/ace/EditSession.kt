package ace

/**
 * https://ace.c9.io/#nav=api&api=edit_session
 */
external interface EditSession {

    fun getDocument(): Document

    fun setMode(modePath: Ace.Mode<*, *>)

}