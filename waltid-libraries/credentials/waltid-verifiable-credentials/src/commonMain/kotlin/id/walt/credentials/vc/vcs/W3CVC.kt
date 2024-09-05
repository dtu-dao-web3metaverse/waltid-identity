package id.walt.credentials.vc.vcs

import id.walt.credentials.schemes.JwsSignatureScheme
import id.walt.credentials.schemes.JwsSignatureScheme.JwsHeader
import id.walt.credentials.schemes.JwsSignatureScheme.JwsOption
import id.walt.crypto.keys.Key
import id.walt.crypto.utils.JsonUtils.toJsonElement
import id.walt.sdjwt.SDJwt
import id.walt.sdjwt.SDMap
import id.walt.sdjwt.SDPayload
import io.ktor.utils.io.core.*
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encodeToString
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.*
import love.forte.plugin.suspendtrans.annotation.JsPromise
import love.forte.plugin.suspendtrans.annotation.JvmAsync
import love.forte.plugin.suspendtrans.annotation.JvmBlocking
import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport

@OptIn(ExperimentalJsExport::class)
@JsExport
class W3CVCSerializer : KSerializer<W3CVC> {
    override val descriptor: SerialDescriptor = JsonObject.serializer().descriptor
    override fun deserialize(decoder: Decoder): W3CVC = W3CVC(decoder.decodeSerializableValue(JsonObject.serializer()))
    override fun serialize(encoder: Encoder, value: W3CVC) = encoder.encodeSerializableValue(JsonObject.serializer(), value.toJsonObject())
}

@OptIn(ExperimentalJsExport::class)
@JsExport
@Serializable(with = W3CVCSerializer::class)
data class W3CVC(
    private val content: Map<String, JsonElement> = emptyMap()
) : Map<String, JsonElement> by content {


    fun getType() = (get("type") ?: error("No `type` in W3C VC!")).jsonArray.map { it.jsonPrimitive.content }

    fun toJsonObject(additionalProperties: Map<String, JsonElement> = emptyMap()): JsonObject
        = JsonObject(content.plus(additionalProperties))
    fun toJson(): String = Json.encodeToString(content)
    fun toPrettyJson(): String = prettyJson.encodeToString(content)

    @JvmBlocking
    @JvmAsync
    @JsPromise
    @JsExport.Ignore
    suspend fun signSdJwt(
        issuerKey: Key,
        issuerKeyId: String,
        subjectDid: String,
        disclosureMap: SDMap,
        /** Set additional options in the JWT header */
        additionalJwtHeaders: Map<String, JsonElement> = emptyMap(),
        /** Set additional options in the JWT payload */
        additionalJwtOptions: Map<String, JsonElement> = emptyMap()
    ): String {
        val vc = this.toJsonObject(additionalJwtOptions)

        val sdPayload = SDPayload.createSDPayload(vc, disclosureMap)
        val signable = Json.encodeToString(sdPayload.undisclosedPayload).toByteArray()

        val signed = issuerKey.signJws(
            signable, mapOf(
                "typ" to "vc+sd-jwt".toJsonElement(),
                "cty" to "credential-claims-set+json".toJsonElement(),
                "kid" to issuerKeyId.toJsonElement()
            ).plus(additionalJwtHeaders)
        )

        return SDJwt.createFromSignedJwt(signed, sdPayload).toString()
    }
    @JvmBlocking
    @JvmAsync
    @JsPromise
    @JsExport.Ignore
    suspend fun signJws(
        issuerKey: Key,
        issuerDid: String?,
        issuerKid: String? = null,
        subjectDid: String,
        /** Set additional options in the JWT header */
        additionalJwtHeader: Map<String, JsonElement> = emptyMap(),
        /** Set additional options in the JWT payload */
        additionalJwtOptions: Map<String, JsonElement> = emptyMap()
    ): String {
        val kid = issuerKid ?: issuerDid ?: issuerKey.getKeyId()

        return JwsSignatureScheme().sign(
            data = this.toJsonObject(),
            key = issuerKey,
            jwtHeaders = mapOf(
                JwsHeader.KEY_ID to kid.toJsonElement(),
                *(additionalJwtHeader.entries.map { it.toPair() }.toTypedArray())
            ),
            jwtOptions = mapOf(
                JwsOption.ISSUER to JsonPrimitive(issuerDid),
                JwsOption.SUBJECT to JsonPrimitive(subjectDid),
                *(additionalJwtOptions.entries.map { it.toPair() }.toTypedArray())
            ),
        )
    }

    companion object {
        fun build(
            context: List<String>,
            type: List<String>,
            vararg data: Pair<String, Any>
        ): W3CVC {
            return W3CVC(
                mutableMapOf(
                    "@context" to context.toJsonElement(),
                    "type" to type.toJsonElement()
                ).apply { putAll(data.toMap().mapValues { it.value.toJsonElement() }) }
            )
        }


        fun fromJson(json: String) =
            W3CVC(Json.decodeFromString<Map<String, JsonElement>>(json))

        private val prettyJson = Json { prettyPrint = true }
    }

}
