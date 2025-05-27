package dev.kdriver.cdp

import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.json.JsonElement
import kotlin.reflect.KClass

/**
 * Thin wrapper of Chrome DevTools Protocol.
 */
interface CDP {

    val events: Flow<Message.Event>
    val responses: Flow<Message.Response>

    val generatedDomains: MutableMap<KClass<out Domain>, Domain>

    suspend fun callCommand(method: String, parameter: JsonElement?): JsonElement?

}
