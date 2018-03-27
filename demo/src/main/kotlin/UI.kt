import ace.ACE
import ace.Editor
import com.github.daemontus.theme.Theme
import com.github.daemontus.theme.ThemeDark
import com.github.daemontus.theme.ThemeLight
import com.github.daemontus.theme.compileCSS
import org.w3c.dom.HTMLAnchorElement
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLInputElement
import org.w3c.files.Blob
import org.w3c.files.BlobPropertyBag
import org.w3c.files.FileReader
import org.w3c.files.get
import kotlin.browser.document
import kotlin.browser.window
import kotlin.dom.addClass
import kotlin.dom.removeClass

/**
 * Initialize user interface for the whole demo.
 */
fun initUserInterface(editor: Editor) {
    // input element for user to edit model file name
    val modelName: MaterialTextFieldElement =
            document.getElementById("model-name")?.unsafeCast<MaterialTextFieldElement>()
                    ?: error("Missing #model-name text field")

    // container for displaying messages to the user
    val toast: MaterialToastElement =
            document.querySelector("#toast")?.unsafeCast<MaterialToastElement>()
                    ?: error("Missing #toast element!")

    initOpenButton(editor, modelName, toast)
    initSaveButton(editor, modelName)
    initThemeToggle(editor)
}

/**
 * Init toggle switch responsible for changing theme from light to dark
 */
private fun initThemeToggle(editor: Editor) {
    // add both themes as ace modules
    val idLight = initAceTheme(ThemeLight)
    val idDark = initAceTheme(ThemeDark)

    // upon toggle change, change theme
    document.querySelector("#theme-switch-checkbox")?.unsafeCast<HTMLInputElement>()?.let { checkbox ->
        val editorCard = document.querySelector("#editor-card")?.unsafeCast<HTMLDivElement>()
        checkbox.addEventListener("change", {
            if (checkbox.checked) {
                editorCard?.addClass("dark")
                editorCard?.removeClass("light")
                editor.setTheme(idDark)
            } else {
                editorCard?.addClass("light")
                editorCard?.removeClass("dark")
                editor.setTheme(idLight)
            }
        })
    }

    editor.setTheme(idDark)
}

/**
 * Define an Ace theme based on a [Theme] object.
 */
private fun initAceTheme(theme: Theme): String {
    val themeID = "ace/theme/${theme.name}"
    ACE.define(themeID, arrayOf("require", "exports", "ace/lib/dom")) { require, module ->
        module.isDark = theme.isDark
        module.cssClass = theme.name
        module.cssText = theme.compileCSS()

        val dom = require("../lib/dom")
        dom.importCssString(module.cssText, module.cssClass)
        module
    }
    return themeID
}

/**
 * Make Open button load files (offline) into the editor.
 */
private fun initOpenButton(editor: Editor, modelName: MaterialTextFieldElement, toast: MaterialToastElement) {
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
                            modelName.setValue(file.name)
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
}

/**
 * Make Save button download text files with appropriate name.
 */
private fun initSaveButton(editor: Editor, modelName: MaterialTextFieldElement) {

    document.getElementById("save-document")?.let { saveButton ->
        saveButton.addEventListener("click", { _ ->
            val editorContent = editor.getSession().getDocument().getValue()
            val fileName = modelName.getValue().trim().takeIf { it.isNotEmpty() } ?: "model.txt"
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