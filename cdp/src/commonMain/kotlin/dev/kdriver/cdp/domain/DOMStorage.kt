package dev.kdriver.cdp.domain

import dev.kaccelero.serializers.Serialization
import dev.kdriver.cdp.CDP
import dev.kdriver.cdp.Domain
import dev.kdriver.cdp.cacheGeneratedDomain
import dev.kdriver.cdp.getGeneratedDomain
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.encodeToJsonElement

public val CDP.dOMStorage: DOMStorage
    get() = getGeneratedDomain() ?: cacheGeneratedDomain(DOMStorage(this))

/**
 * Query and modify DOM storage.
 */
public class DOMStorage(
    private val cdp: CDP,
) : Domain {
    public val domStorageItemAdded: Flow<DomStorageItemAddedParameter> = cdp
        .events
        .filter { it.method == "DOMStorage.domStorageItemAdded" }
        .map { it.params }
        .filterNotNull()
        .map { Serialization.json.decodeFromJsonElement(it) }

    public val domStorageItemRemoved: Flow<DomStorageItemRemovedParameter> = cdp
        .events
        .filter { it.method == "DOMStorage.domStorageItemRemoved" }
        .map { it.params }
        .filterNotNull()
        .map { Serialization.json.decodeFromJsonElement(it) }

    public val domStorageItemUpdated: Flow<DomStorageItemUpdatedParameter> = cdp
        .events
        .filter { it.method == "DOMStorage.domStorageItemUpdated" }
        .map { it.params }
        .filterNotNull()
        .map { Serialization.json.decodeFromJsonElement(it) }

    public val domStorageItemsCleared: Flow<DomStorageItemsClearedParameter> = cdp
        .events
        .filter { it.method == "DOMStorage.domStorageItemsCleared" }
        .map { it.params }
        .filterNotNull()
        .map { Serialization.json.decodeFromJsonElement(it) }

    public suspend fun clear(args: ClearParameter) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("DOMStorage.clear", parameter)
    }

    public suspend fun clear(storageId: StorageId) {
        val parameter = ClearParameter(storageId = storageId)
        clear(parameter)
    }

    /**
     * Disables storage tracking, prevents storage events from being sent to the client.
     */
    public suspend fun disable() {
        val parameter = null
        cdp.callCommand("DOMStorage.disable", parameter)
    }

    /**
     * Enables storage tracking, storage events will now be delivered to the client.
     */
    public suspend fun enable() {
        val parameter = null
        cdp.callCommand("DOMStorage.enable", parameter)
    }

    public suspend fun getDOMStorageItems(args: GetDOMStorageItemsParameter): GetDOMStorageItemsReturn {
        val parameter = Serialization.json.encodeToJsonElement(args)
        val result = cdp.callCommand("DOMStorage.getDOMStorageItems", parameter)
        return result!!.let { Serialization.json.decodeFromJsonElement(it) }
    }

    public suspend fun getDOMStorageItems(storageId: StorageId): GetDOMStorageItemsReturn {
        val parameter = GetDOMStorageItemsParameter(storageId = storageId)
        return getDOMStorageItems(parameter)
    }

    public suspend fun removeDOMStorageItem(args: RemoveDOMStorageItemParameter) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("DOMStorage.removeDOMStorageItem", parameter)
    }

    public suspend fun removeDOMStorageItem(storageId: StorageId, key: String) {
        val parameter = RemoveDOMStorageItemParameter(storageId = storageId, key = key)
        removeDOMStorageItem(parameter)
    }

    public suspend fun setDOMStorageItem(args: SetDOMStorageItemParameter) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("DOMStorage.setDOMStorageItem", parameter)
    }

    public suspend fun setDOMStorageItem(
        storageId: StorageId,
        key: String,
        `value`: String,
    ) {
        val parameter = SetDOMStorageItemParameter(storageId = storageId, key = key, value = value)
        setDOMStorageItem(parameter)
    }

    /**
     * DOM Storage identifier.
     */
    @Serializable
    public data class StorageId(
        /**
         * Security origin for the storage.
         */
        public val securityOrigin: String? = null,
        /**
         * Represents a key by which DOM Storage keys its CachedStorageAreas
         */
        public val storageKey: String? = null,
        /**
         * Whether the storage is local storage (not session storage).
         */
        public val isLocalStorage: Boolean,
    )

    @Serializable
    public data class DomStorageItemAddedParameter(
        public val storageId: StorageId,
        public val key: String,
        public val newValue: String,
    )

    @Serializable
    public data class DomStorageItemRemovedParameter(
        public val storageId: StorageId,
        public val key: String,
    )

    @Serializable
    public data class DomStorageItemUpdatedParameter(
        public val storageId: StorageId,
        public val key: String,
        public val oldValue: String,
        public val newValue: String,
    )

    @Serializable
    public data class DomStorageItemsClearedParameter(
        public val storageId: StorageId,
    )

    @Serializable
    public data class ClearParameter(
        public val storageId: StorageId,
    )

    @Serializable
    public data class GetDOMStorageItemsParameter(
        public val storageId: StorageId,
    )

    @Serializable
    public data class GetDOMStorageItemsReturn(
        public val entries: List<List<Double>>,
    )

    @Serializable
    public data class RemoveDOMStorageItemParameter(
        public val storageId: StorageId,
        public val key: String,
    )

    @Serializable
    public data class SetDOMStorageItemParameter(
        public val storageId: StorageId,
        public val key: String,
        public val `value`: String,
    )
}
