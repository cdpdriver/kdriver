package dev.kdriver.cdp

import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.json.JsonElement
import kotlin.reflect.KClass

/**
 * Thin wrapper of Chrome DevTools Protocol.
 */
interface CDP {

    @InternalCdpApi
    val events: Flow<Message.Event>

    @InternalCdpApi
    val responses: Flow<Message.Response>

    @InternalCdpApi
    val generatedDomains: MutableMap<KClass<out Domain>, Domain>

    @InternalCdpApi
    suspend fun callCommand(method: String, parameter: JsonElement?): JsonElement?

}
