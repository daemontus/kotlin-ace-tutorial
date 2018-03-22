package ace

/**
 * https://ace.c9.io/#nav=api&api=document
 */
external interface Document {

    fun getValue(): String
    fun setValue(value: String)

}