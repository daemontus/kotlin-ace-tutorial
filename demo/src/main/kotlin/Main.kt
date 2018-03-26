import ace.*

/**
* Here, we initialise the whole demo.
*/
@JsName("initDemo")
fun initDemo() {
    val editor: Editor = ACE.edit("editor") // Start Ace editor
    initUserInterface(editor)
    initEditorMode(editor)
}

private fun initEditorMode(editor: Editor) {
    val mode = ODE.Mode(editor)
    editor.getSession().setMode(mode)
}