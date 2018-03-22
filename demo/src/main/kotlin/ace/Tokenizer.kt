package ace

import com.github.daemontus.tokenizer.AceToken

/**
 * https://ace.c9.io/#nav=api&api=tokenizer
 */
external interface Tokenizer<Token: AceToken, State: Any?> {

    @JsName("getLineTokens")
    fun getLineTokens(line: String, stateStart: State?): Tokens<Token, State>

}