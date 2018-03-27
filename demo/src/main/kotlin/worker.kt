import ace.*
import com.github.daemontus.tokenizer.OdeParser
import com.github.daemontus.tokenizer.OdeTokenizer
import com.github.daemontus.tokenizer.Rules

const val WORKER_PATH = "ace/mode/ode_worker"

abstract class Worker(private val sender: Any?) : Mirror {

    private val tokenizer = OdeTokenizer()

    init {
        initSuper1Arg(ace.require<MirrorModule>(Module.mirror()).mirror, sender)
        setTimeout(250)
    }

    override fun onUpdate() {
        val value = this.document.getValue()
        println("Update:")
        println(value)
        val result = HashMap<String, String>()
        value.lines().fold<String, List<OdeTokenizer.State>?>(null, { state, line ->
            val (tokens, nextState) = tokenizer.tokenizeLine(line, state ?: emptyList())
            result.putAll(OdeParser.inferTypes(tokens).filterValues { it != Rules.Identifier.Local.id })
            nextState
        })
        println("Types: $result")
        sender.asDynamic().emit("type-hints", JSON.stringify(result.toList().toTypedArray()))
    }
}

@JsName("initWorker")
fun initWorker() {
    define(WORKER_PATH, arrayOf("require", "exports", Module.mirror()), { _, exports ->
        exports[Worker::class.js.name] = Worker::class.js
        exports
    })
}