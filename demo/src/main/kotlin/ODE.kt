import ace.Ace
import ace.Tokens
import com.github.daemontus.tokenizer.OdeToken
import com.github.daemontus.tokenizer.OdeTokenizer

object ODE {

    object Mode : Ace.Mode<OdeToken, List<OdeTokenizer.State>> {
        // initialize this after OOP is called!
        override var tokenizer: ace.Tokenizer<OdeToken, List<OdeTokenizer.State>>? = null
    }

    object Tokenizer : OdeTokenizer(), ace.Tokenizer<OdeToken, List<OdeTokenizer.State>> {
        override fun getLineTokens(line: String, stateStart: List<State>?): Tokens<OdeToken, List<State>> {
            val (tokens, state) = tokenizeLine(line, stateStart ?: emptyList())
            return Tokens(tokens.toTypedArray(), state)
        }
    }

}