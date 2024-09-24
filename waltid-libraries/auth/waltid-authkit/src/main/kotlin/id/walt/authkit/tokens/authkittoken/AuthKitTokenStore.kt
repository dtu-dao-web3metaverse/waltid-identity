package id.walt.authkit.tokens.authkittoken

interface AuthKitTokenStore {

    /**
     * Return session id
     */
    fun mapToken(token: String, sessionId: String)

    fun getTokenSessionId(token: String): String

    fun validateToken(token: String): Boolean

    fun dropToken(token: String)



}
