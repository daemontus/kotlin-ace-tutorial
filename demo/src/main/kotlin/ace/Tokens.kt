package ace

import com.github.daemontus.tokenizer.AceToken


/**
 * Return data type of [Tokenizer.getLineTokens].
 */
@Suppress("unused")     // called from JS
class Tokens<Token : AceToken, out State: Any?>(
        @JsName("tokens")
        val tokens: Array<Token>,
        @JsName("state")
        val state: State?
)