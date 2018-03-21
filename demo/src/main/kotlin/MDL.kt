import org.w3c.dom.Element
import org.w3c.dom.HTMLInputElement

/* Mechanism for displaying toasts and messages. */

data class Message(
        val message: String,
        val timeout: Int = 5000,
        val actionText: String? = null,
        val actionHandler: (() -> Unit)? = null
)

external interface MaterialToastElement {

    @JsName("MaterialSnackbar")
    val materialSnackBar: MaterialToast

}

external interface MaterialToast {

    @JsName("showSnackbar")
    fun showToast(data: Message)
}

fun MaterialToastElement.showToast(message: String, timeout: Int = 5000) {
    this.materialSnackBar.showToast(Message(message, timeout))
}

/* Mechanisms for safely setting and getting values of material text fields, since they are a little peculiar */

fun MaterialTextFieldElement.setValue(value: String) {
    this.materialTextField.change(value)
}

fun MaterialTextFieldElement.getValue(): String =
        this.querySelector(".mdl-textfield__input")?.unsafeCast<HTMLInputElement>()?.value ?: ""

external interface MaterialTextField {

    @JsName("change")
    fun change(value: String)

}

external interface MaterialTextFieldElement {

    @JsName("MaterialTextfield")
    val materialTextField: MaterialTextField

    fun querySelector(elementId: String): Element?

}