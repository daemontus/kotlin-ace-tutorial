import com.github.daemontus.tokenizer.parseLines
import com.github.daemontus.validate
import java.io.File

fun main(args: Array<String>) {
    val f = File("/Users/daemontus/Downloads/clark.ode")
    f.useLines { lines ->
        val linesList = lines.toList()
        val modelFile = linesList.parseLines()
        println(modelFile)
        val model = modelFile.validate()
        println(model)
    }
}