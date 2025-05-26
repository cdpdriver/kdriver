package dev.kdriver.cdp

import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.StructureKind
import kotlinx.serialization.descriptors.buildSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement

internal object MessageSerializer : KSerializer<Message> {

    override fun deserialize(decoder: Decoder): Message {
        require(decoder is JsonDecoder)
        val element = decoder.decodeJsonElement()
        require(element is JsonObject)
        return if (element.containsKey("id")) {
            Json.decodeFromJsonElement<Message.Response>(element)
        } else {
            Json.decodeFromJsonElement<Message.Event>(element)
        }
    }

    @OptIn(InternalSerializationApi::class)
    override val descriptor: SerialDescriptor
        get() = buildSerialDescriptor("Message", StructureKind.OBJECT)

    override fun serialize(encoder: Encoder, value: Message) {
        error("This serializer is only for deserialization!")
    }

}
