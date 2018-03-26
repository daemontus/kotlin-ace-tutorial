import ace.*
import ace.internal.*
import com.github.daemontus.tokenizer.OdeTokenizer
import kotlin.browser.window

object ODE {

    class Mode(
            private val editor: Editor
    ) : TextMode {

        override val tokenizer: ace.Tokenizer<*, *> = Tokenizer()

        init {
            initSuper0Arg(ACE.require<TextModeModule>(Module.Internal.textMode()).mode)
        }

        override fun createWorker(session: EditSession): WorkerClient {
            // stripping down path until only prefix remains
            // so http://domain.tld/foo/goo/file.html?query becomes http://domain.tld/foo/goo
            val prefix = window.location.href.replace(Regex("[^/]*\$"), "")

            val initData = WorkerInit(
                    dependencies = arrayOf(
                            "$prefix/lib/kotlin-stdlib-js-1.2.30/kotlin.js",
                            "$prefix/lib/kotlin-ace-tutorial-js/kotlin-ace-tutorial-js.js",
                            "$prefix/lib/common-0.0.2/common.js",
                            "$prefix/lib/worker-0.0.2/worker.js",
                            "$prefix/lib/main-0.0.2/main.js",
                            "$prefix/lib/demo.js",
                            "$prefix/lib/worker-0.0.2/ace-worker.js"    // always load last
                    ),
                    module = "demo",
                    mainMethod = "initWorker"
            )

            val workerModule = ACE.require<WorkerClientModule>(Module.Internal.workerClient()).workerClient

            val client = workerModule.new(
                    topLevelNamespaces = arrayOf("ace"),
                    mod = WORKER_PATH,
                    classname = Worker::class.js.name,
                    workerUrl = "$prefix/lib/worker-0.0.2/worker-init.js",
                    importScripts = JSON.stringify(initData)
            )

            client.attachToDocument(editor.getSession().getDocument())
            return client
        }

    }

    class Tokenizer : OdeTokenizer(), ace.Tokenizer<Tokenizer.Token, List<OdeTokenizer.State>> {

        init {
            initSuper0Arg(ACE.require<TokenizerModule>(Module.tokenizer()).tokenizer)
        }

        class Token(override val type: String, override val value: String) : ace.Tokenizer.Token

        class Tokens(
                override val tokens: Array<Token>,
                override val state: List<State>?
        ) : ace.Tokenizer.Tokens<Token, List<OdeTokenizer.State>>

        override fun getLineTokens(line: String, startState: List<State>?): Tokens {
            val (tokens, state) = tokenizeLine(line, startState ?: emptyList())
            return Tokens(tokens.map { Token(it.type, it.value) }.toTypedArray(), state)
        }

    }

}