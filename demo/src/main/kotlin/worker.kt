import ace.*

const val WORKER_PATH = "ace/mode/ode_worker"

abstract class Worker(sender: Any?) : Mirror {

    init {
        initSuper1Arg(ace.require<MirrorModule>(Module.mirror()).mirror, sender)
        //initSuper(require<MirrorModule>(MIRROR_PATH).mirror, sender)
        setTimeout(250)
    }

    override fun onUpdate() {
        val value = this.document.getValue()
        println("Update:")
        println(value)
    }
}

@JsName("initWorker")
fun initWorker() {
    define(WORKER_PATH, arrayOf("require", "exports", Module.mirror()), { _, exports ->
        exports[Worker::class.js.name] = Worker::class.js
        exports
    })
}