import org.w3c.dom.HTMLAnchorElement
import org.w3c.dom.HTMLInputElement
import org.w3c.files.Blob
import org.w3c.files.BlobPropertyBag
import org.w3c.files.FileReader
import org.w3c.files.get
import kotlin.browser.document
import kotlin.browser.window

external val ace: Ace

/**
 * Here, we initialise the whole demo.
 */
fun main(args: Array<String>) {
    val editor = ace.edit("editor") // Init Ace editor

    // container for displaying messages to the user
    val toast = document.querySelector("#toast")?.unsafeCast<MaterialToastElement>() ?: error("Missing #toast element!")

    // input element for user to edit model file name
    val modelName = document.getElementById("model-name")?.unsafeCast<MaterialTextFieldElement>()

    document.getElementById("open-document")?.unsafeCast<HTMLInputElement>()?.let { fileInput ->
        fileInput.addEventListener("change", {
            // Every time a file is selected, read it's contents into the editor.
            fileInput.files?.let { files ->
                files[files.length - 1]?.let { file ->
                    if (file.size > 10 * 1000 * 1000) {
                        toast.showToast("File size exceeds 10MB!")
                    } else {
                        val reader = FileReader()
                        reader.onloadend = { _ ->
                            fileInput.value = ""    // clear file input field
                            editor.getSession().getDocument().setValue(reader.result as String)
                            modelName?.setValue(file.name)
                            toast.showToast("${file.name} loaded.")
                        }
                        reader.onerror = { _ ->
                            toast.showToast("Error loading ${file.name}: ${reader.error}")
                        }
                        reader.readAsText(file)
                    }
                }
            }
        })
    }

    document.getElementById("save-document")?.let { saveButton ->
        saveButton.addEventListener("click", { _ ->
            val editorContent = editor.getSession().getDocument().getValue()
            val fileName = modelName?.getValue() ?: "model.txt"
            val textBlob = Blob(arrayOf(editorContent), BlobPropertyBag("text/plain"))

            val link = document.createElement("a").unsafeCast<HTMLAnchorElement>()
            link.download = fileName
            link.innerHTML = "Download file"
            val window = window.asDynamic()
            if (window.webkitURL != null) {
                // Chrome allows the link to be clicked without actually adding it to the DOM.
                link.href = window.webkitURL.createObjectURL(textBlob) as String
            } else {
                // Firefox requires the link to be added to the DOM before it can be clicked.
                link.href = window.URL.createObjectURL(textBlob) as String
                link.onclick = { _ -> document.body?.removeChild(link) }
                link.style.display = "none"
                document.body?.appendChild(link)
            }
            link.click()
        })
    }
}