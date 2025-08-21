@file:Suppress("ALL")

package dev.kdriver.cdp.domain

import dev.kaccelero.serializers.Serialization
import dev.kdriver.cdp.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.encodeToJsonElement

/**
 * Defines commands and events for browser extensions.
 */
public val CDP.extensions: Extensions
    get() = getGeneratedDomain() ?: cacheGeneratedDomain(Extensions(this))

/**
 * Defines commands and events for browser extensions.
 */
public class Extensions(
    private val cdp: CDP,
) : Domain {
    /**
     * Installs an unpacked extension from the filesystem similar to
     * --load-extension CLI flags. Returns extension ID once the extension
     * has been installed. Available if the client is connected using the
     * --remote-debugging-pipe flag and the --enable-unsafe-extension-debugging
     * flag is set.
     */
    public suspend fun loadUnpacked(
        args: LoadUnpackedParameter,
        mode: CommandMode = CommandMode.DEFAULT,
    ): LoadUnpackedReturn {
        val parameter = Serialization.json.encodeToJsonElement(args)
        val result = cdp.callCommand("Extensions.loadUnpacked", parameter, mode)
        return result!!.let { Serialization.json.decodeFromJsonElement(it) }
    }

    /**
     * Installs an unpacked extension from the filesystem similar to
     * --load-extension CLI flags. Returns extension ID once the extension
     * has been installed. Available if the client is connected using the
     * --remote-debugging-pipe flag and the --enable-unsafe-extension-debugging
     * flag is set.
     *
     * @param path Absolute file path.
     */
    public suspend fun loadUnpacked(path: String): LoadUnpackedReturn {
        val parameter = LoadUnpackedParameter(path = path)
        return loadUnpacked(parameter)
    }

    /**
     * Uninstalls an unpacked extension (others not supported) from the profile.
     * Available if the client is connected using the --remote-debugging-pipe flag
     * and the --enable-unsafe-extension-debugging.
     */
    public suspend fun uninstall(args: UninstallParameter, mode: CommandMode = CommandMode.DEFAULT) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("Extensions.uninstall", parameter, mode)
    }

    /**
     * Uninstalls an unpacked extension (others not supported) from the profile.
     * Available if the client is connected using the --remote-debugging-pipe flag
     * and the --enable-unsafe-extension-debugging.
     *
     * @param id Extension id.
     */
    public suspend fun uninstall(id: String) {
        val parameter = UninstallParameter(id = id)
        uninstall(parameter)
    }

    /**
     * Gets data from extension storage in the given `storageArea`. If `keys` is
     * specified, these are used to filter the result.
     */
    public suspend fun getStorageItems(
        args: GetStorageItemsParameter,
        mode: CommandMode = CommandMode.DEFAULT,
    ): GetStorageItemsReturn {
        val parameter = Serialization.json.encodeToJsonElement(args)
        val result = cdp.callCommand("Extensions.getStorageItems", parameter, mode)
        return result!!.let { Serialization.json.decodeFromJsonElement(it) }
    }

    /**
     * Gets data from extension storage in the given `storageArea`. If `keys` is
     * specified, these are used to filter the result.
     *
     * @param id ID of extension.
     * @param storageArea StorageArea to retrieve data from.
     * @param keys Keys to retrieve.
     */
    public suspend fun getStorageItems(
        id: String,
        storageArea: StorageArea,
        keys: List<String>? = null,
    ): GetStorageItemsReturn {
        val parameter = GetStorageItemsParameter(id = id, storageArea = storageArea, keys = keys)
        return getStorageItems(parameter)
    }

    /**
     * Removes `keys` from extension storage in the given `storageArea`.
     */
    public suspend fun removeStorageItems(args: RemoveStorageItemsParameter, mode: CommandMode = CommandMode.DEFAULT) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("Extensions.removeStorageItems", parameter, mode)
    }

    /**
     * Removes `keys` from extension storage in the given `storageArea`.
     *
     * @param id ID of extension.
     * @param storageArea StorageArea to remove data from.
     * @param keys Keys to remove.
     */
    public suspend fun removeStorageItems(
        id: String,
        storageArea: StorageArea,
        keys: List<String>,
    ) {
        val parameter = RemoveStorageItemsParameter(id = id, storageArea = storageArea, keys = keys)
        removeStorageItems(parameter)
    }

    /**
     * Clears extension storage in the given `storageArea`.
     */
    public suspend fun clearStorageItems(args: ClearStorageItemsParameter, mode: CommandMode = CommandMode.DEFAULT) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("Extensions.clearStorageItems", parameter, mode)
    }

    /**
     * Clears extension storage in the given `storageArea`.
     *
     * @param id ID of extension.
     * @param storageArea StorageArea to remove data from.
     */
    public suspend fun clearStorageItems(id: String, storageArea: StorageArea) {
        val parameter = ClearStorageItemsParameter(id = id, storageArea = storageArea)
        clearStorageItems(parameter)
    }

    /**
     * Sets `values` in extension storage in the given `storageArea`. The provided `values`
     * will be merged with existing values in the storage area.
     */
    public suspend fun setStorageItems(args: SetStorageItemsParameter, mode: CommandMode = CommandMode.DEFAULT) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("Extensions.setStorageItems", parameter, mode)
    }

    /**
     * Sets `values` in extension storage in the given `storageArea`. The provided `values`
     * will be merged with existing values in the storage area.
     *
     * @param id ID of extension.
     * @param storageArea StorageArea to set data in.
     * @param values Values to set.
     */
    public suspend fun setStorageItems(
        id: String,
        storageArea: StorageArea,
        values: Map<String, JsonElement>,
    ) {
        val parameter = SetStorageItemsParameter(id = id, storageArea = storageArea, values = values)
        setStorageItems(parameter)
    }

    /**
     * Storage areas.
     */
    @Serializable
    public enum class StorageArea {
        @SerialName("session")
        SESSION,

        @SerialName("local")
        LOCAL,

        @SerialName("sync")
        SYNC,

        @SerialName("managed")
        MANAGED,
    }

    @Serializable
    public data class LoadUnpackedParameter(
        /**
         * Absolute file path.
         */
        public val path: String,
    )

    @Serializable
    public data class LoadUnpackedReturn(
        /**
         * Extension id.
         */
        public val id: String,
    )

    @Serializable
    public data class UninstallParameter(
        /**
         * Extension id.
         */
        public val id: String,
    )

    @Serializable
    public data class GetStorageItemsParameter(
        /**
         * ID of extension.
         */
        public val id: String,
        /**
         * StorageArea to retrieve data from.
         */
        public val storageArea: StorageArea,
        /**
         * Keys to retrieve.
         */
        public val keys: List<String>? = null,
    )

    @Serializable
    public data class GetStorageItemsReturn(
        public val `data`: Map<String, JsonElement>,
    )

    @Serializable
    public data class RemoveStorageItemsParameter(
        /**
         * ID of extension.
         */
        public val id: String,
        /**
         * StorageArea to remove data from.
         */
        public val storageArea: StorageArea,
        /**
         * Keys to remove.
         */
        public val keys: List<String>,
    )

    @Serializable
    public data class ClearStorageItemsParameter(
        /**
         * ID of extension.
         */
        public val id: String,
        /**
         * StorageArea to remove data from.
         */
        public val storageArea: StorageArea,
    )

    @Serializable
    public data class SetStorageItemsParameter(
        /**
         * ID of extension.
         */
        public val id: String,
        /**
         * StorageArea to set data in.
         */
        public val storageArea: StorageArea,
        /**
         * Values to set.
         */
        public val values: Map<String, JsonElement>,
    )
}
