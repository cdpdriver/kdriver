@file:Suppress("ALL")

package dev.kdriver.cdp.domain

import dev.kaccelero.serializers.Serialization
import dev.kdriver.cdp.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.encodeToJsonElement

public val CDP.indexedDB: IndexedDB
    get() = getGeneratedDomain() ?: cacheGeneratedDomain(IndexedDB(this))

public class IndexedDB(
    private val cdp: CDP,
) : Domain {
    /**
     * Clears all entries from an object store.
     */
    public suspend fun clearObjectStore(args: ClearObjectStoreParameter, mode: CommandMode = CommandMode.DEFAULT) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("IndexedDB.clearObjectStore", parameter, mode)
    }

    /**
     * Clears all entries from an object store.
     *
     * @param securityOrigin At least and at most one of securityOrigin, storageKey, or storageBucket must be specified.
     * Security origin.
     * @param storageKey Storage key.
     * @param storageBucket Storage bucket. If not specified, it uses the default bucket.
     * @param databaseName Database name.
     * @param objectStoreName Object store name.
     */
    public suspend fun clearObjectStore(
        securityOrigin: String? = null,
        storageKey: String? = null,
        storageBucket: Storage.StorageBucket? = null,
        databaseName: String,
        objectStoreName: String,
    ) {
        val parameter = ClearObjectStoreParameter(
            securityOrigin = securityOrigin,
            storageKey = storageKey,
            storageBucket = storageBucket,
            databaseName = databaseName,
            objectStoreName = objectStoreName
        )
        clearObjectStore(parameter)
    }

    /**
     * Deletes a database.
     */
    public suspend fun deleteDatabase(args: DeleteDatabaseParameter, mode: CommandMode = CommandMode.DEFAULT) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("IndexedDB.deleteDatabase", parameter, mode)
    }

    /**
     * Deletes a database.
     *
     * @param securityOrigin At least and at most one of securityOrigin, storageKey, or storageBucket must be specified.
     * Security origin.
     * @param storageKey Storage key.
     * @param storageBucket Storage bucket. If not specified, it uses the default bucket.
     * @param databaseName Database name.
     */
    public suspend fun deleteDatabase(
        securityOrigin: String? = null,
        storageKey: String? = null,
        storageBucket: Storage.StorageBucket? = null,
        databaseName: String,
    ) {
        val parameter = DeleteDatabaseParameter(
            securityOrigin = securityOrigin,
            storageKey = storageKey,
            storageBucket = storageBucket,
            databaseName = databaseName
        )
        deleteDatabase(parameter)
    }

    /**
     * Delete a range of entries from an object store
     */
    public suspend fun deleteObjectStoreEntries(
        args: DeleteObjectStoreEntriesParameter,
        mode: CommandMode = CommandMode.DEFAULT,
    ) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("IndexedDB.deleteObjectStoreEntries", parameter, mode)
    }

    /**
     * Delete a range of entries from an object store
     *
     * @param securityOrigin At least and at most one of securityOrigin, storageKey, or storageBucket must be specified.
     * Security origin.
     * @param storageKey Storage key.
     * @param storageBucket Storage bucket. If not specified, it uses the default bucket.
     * @param databaseName No description
     * @param objectStoreName No description
     * @param keyRange Range of entry keys to delete
     */
    public suspend fun deleteObjectStoreEntries(
        securityOrigin: String? = null,
        storageKey: String? = null,
        storageBucket: Storage.StorageBucket? = null,
        databaseName: String,
        objectStoreName: String,
        keyRange: KeyRange,
    ) {
        val parameter = DeleteObjectStoreEntriesParameter(
            securityOrigin = securityOrigin,
            storageKey = storageKey,
            storageBucket = storageBucket,
            databaseName = databaseName,
            objectStoreName = objectStoreName,
            keyRange = keyRange
        )
        deleteObjectStoreEntries(parameter)
    }

    /**
     * Disables events from backend.
     */
    public suspend fun disable(mode: CommandMode = CommandMode.DEFAULT) {
        val parameter = null
        cdp.callCommand("IndexedDB.disable", parameter, mode)
    }

    /**
     * Enables events from backend.
     */
    public suspend fun enable(mode: CommandMode = CommandMode.DEFAULT) {
        val parameter = null
        cdp.callCommand("IndexedDB.enable", parameter, mode)
    }

    /**
     * Requests data from object store or index.
     */
    public suspend fun requestData(
        args: RequestDataParameter,
        mode: CommandMode = CommandMode.DEFAULT,
    ): RequestDataReturn {
        val parameter = Serialization.json.encodeToJsonElement(args)
        val result = cdp.callCommand("IndexedDB.requestData", parameter, mode)
        return result!!.let { Serialization.json.decodeFromJsonElement(it) }
    }

    /**
     * Requests data from object store or index.
     *
     * @param securityOrigin At least and at most one of securityOrigin, storageKey, or storageBucket must be specified.
     * Security origin.
     * @param storageKey Storage key.
     * @param storageBucket Storage bucket. If not specified, it uses the default bucket.
     * @param databaseName Database name.
     * @param objectStoreName Object store name.
     * @param indexName Index name, empty string for object store data requests.
     * @param skipCount Number of records to skip.
     * @param pageSize Number of records to fetch.
     * @param keyRange Key range.
     */
    public suspend fun requestData(
        securityOrigin: String? = null,
        storageKey: String? = null,
        storageBucket: Storage.StorageBucket? = null,
        databaseName: String,
        objectStoreName: String,
        indexName: String,
        skipCount: Int,
        pageSize: Int,
        keyRange: KeyRange? = null,
    ): RequestDataReturn {
        val parameter = RequestDataParameter(
            securityOrigin = securityOrigin,
            storageKey = storageKey,
            storageBucket = storageBucket,
            databaseName = databaseName,
            objectStoreName = objectStoreName,
            indexName = indexName,
            skipCount = skipCount,
            pageSize = pageSize,
            keyRange = keyRange
        )
        return requestData(parameter)
    }

    /**
     * Gets metadata of an object store.
     */
    public suspend fun getMetadata(
        args: GetMetadataParameter,
        mode: CommandMode = CommandMode.DEFAULT,
    ): GetMetadataReturn {
        val parameter = Serialization.json.encodeToJsonElement(args)
        val result = cdp.callCommand("IndexedDB.getMetadata", parameter, mode)
        return result!!.let { Serialization.json.decodeFromJsonElement(it) }
    }

    /**
     * Gets metadata of an object store.
     *
     * @param securityOrigin At least and at most one of securityOrigin, storageKey, or storageBucket must be specified.
     * Security origin.
     * @param storageKey Storage key.
     * @param storageBucket Storage bucket. If not specified, it uses the default bucket.
     * @param databaseName Database name.
     * @param objectStoreName Object store name.
     */
    public suspend fun getMetadata(
        securityOrigin: String? = null,
        storageKey: String? = null,
        storageBucket: Storage.StorageBucket? = null,
        databaseName: String,
        objectStoreName: String,
    ): GetMetadataReturn {
        val parameter = GetMetadataParameter(
            securityOrigin = securityOrigin,
            storageKey = storageKey,
            storageBucket = storageBucket,
            databaseName = databaseName,
            objectStoreName = objectStoreName
        )
        return getMetadata(parameter)
    }

    /**
     * Requests database with given name in given frame.
     */
    public suspend fun requestDatabase(
        args: RequestDatabaseParameter,
        mode: CommandMode = CommandMode.DEFAULT,
    ): RequestDatabaseReturn {
        val parameter = Serialization.json.encodeToJsonElement(args)
        val result = cdp.callCommand("IndexedDB.requestDatabase", parameter, mode)
        return result!!.let { Serialization.json.decodeFromJsonElement(it) }
    }

    /**
     * Requests database with given name in given frame.
     *
     * @param securityOrigin At least and at most one of securityOrigin, storageKey, or storageBucket must be specified.
     * Security origin.
     * @param storageKey Storage key.
     * @param storageBucket Storage bucket. If not specified, it uses the default bucket.
     * @param databaseName Database name.
     */
    public suspend fun requestDatabase(
        securityOrigin: String? = null,
        storageKey: String? = null,
        storageBucket: Storage.StorageBucket? = null,
        databaseName: String,
    ): RequestDatabaseReturn {
        val parameter = RequestDatabaseParameter(
            securityOrigin = securityOrigin,
            storageKey = storageKey,
            storageBucket = storageBucket,
            databaseName = databaseName
        )
        return requestDatabase(parameter)
    }

    /**
     * Requests database names for given security origin.
     */
    public suspend fun requestDatabaseNames(
        args: RequestDatabaseNamesParameter,
        mode: CommandMode = CommandMode.DEFAULT,
    ): RequestDatabaseNamesReturn {
        val parameter = Serialization.json.encodeToJsonElement(args)
        val result = cdp.callCommand("IndexedDB.requestDatabaseNames", parameter, mode)
        return result!!.let { Serialization.json.decodeFromJsonElement(it) }
    }

    /**
     * Requests database names for given security origin.
     *
     * @param securityOrigin At least and at most one of securityOrigin, storageKey, or storageBucket must be specified.
     * Security origin.
     * @param storageKey Storage key.
     * @param storageBucket Storage bucket. If not specified, it uses the default bucket.
     */
    public suspend fun requestDatabaseNames(
        securityOrigin: String? = null,
        storageKey: String? = null,
        storageBucket: Storage.StorageBucket? = null,
    ): RequestDatabaseNamesReturn {
        val parameter = RequestDatabaseNamesParameter(
            securityOrigin = securityOrigin,
            storageKey = storageKey,
            storageBucket = storageBucket
        )
        return requestDatabaseNames(parameter)
    }

    /**
     * Database with an array of object stores.
     */
    @Serializable
    public data class DatabaseWithObjectStores(
        /**
         * Database name.
         */
        public val name: String,
        /**
         * Database version (type is not 'integer', as the standard
         * requires the version number to be 'unsigned long long')
         */
        public val version: Double,
        /**
         * Object stores in this database.
         */
        public val objectStores: List<ObjectStore>,
    )

    /**
     * Object store.
     */
    @Serializable
    public data class ObjectStore(
        /**
         * Object store name.
         */
        public val name: String,
        /**
         * Object store key path.
         */
        public val keyPath: KeyPath,
        /**
         * If true, object store has auto increment flag set.
         */
        public val autoIncrement: Boolean,
        /**
         * Indexes in this object store.
         */
        public val indexes: List<ObjectStoreIndex>,
    )

    /**
     * Object store index.
     */
    @Serializable
    public data class ObjectStoreIndex(
        /**
         * Index name.
         */
        public val name: String,
        /**
         * Index key path.
         */
        public val keyPath: KeyPath,
        /**
         * If true, index is unique.
         */
        public val unique: Boolean,
        /**
         * If true, index allows multiple entries for a key.
         */
        public val multiEntry: Boolean,
    )

    /**
     * Key.
     */
    @Serializable
    public data class Key(
        /**
         * Key type.
         */
        public val type: String,
        /**
         * Number value.
         */
        public val number: Double? = null,
        /**
         * String value.
         */
        public val string: String? = null,
        /**
         * Date value.
         */
        public val date: Double? = null,
        /**
         * Array value.
         */
        public val array: List<Key>? = null,
    )

    /**
     * Key range.
     */
    @Serializable
    public data class KeyRange(
        /**
         * Lower bound.
         */
        public val lower: Key? = null,
        /**
         * Upper bound.
         */
        public val upper: Key? = null,
        /**
         * If true lower bound is open.
         */
        public val lowerOpen: Boolean,
        /**
         * If true upper bound is open.
         */
        public val upperOpen: Boolean,
    )

    /**
     * Data entry.
     */
    @Serializable
    public data class DataEntry(
        /**
         * Key object.
         */
        public val key: Runtime.RemoteObject,
        /**
         * Primary key object.
         */
        public val primaryKey: Runtime.RemoteObject,
        /**
         * Value object.
         */
        public val `value`: Runtime.RemoteObject,
    )

    /**
     * Key path.
     */
    @Serializable
    public data class KeyPath(
        /**
         * Key path type.
         */
        public val type: String,
        /**
         * String value.
         */
        public val string: String? = null,
        /**
         * Array value.
         */
        public val array: List<String>? = null,
    )

    @Serializable
    public data class ClearObjectStoreParameter(
        /**
         * At least and at most one of securityOrigin, storageKey, or storageBucket must be specified.
         * Security origin.
         */
        public val securityOrigin: String? = null,
        /**
         * Storage key.
         */
        public val storageKey: String? = null,
        /**
         * Storage bucket. If not specified, it uses the default bucket.
         */
        public val storageBucket: Storage.StorageBucket? = null,
        /**
         * Database name.
         */
        public val databaseName: String,
        /**
         * Object store name.
         */
        public val objectStoreName: String,
    )

    @Serializable
    public data class DeleteDatabaseParameter(
        /**
         * At least and at most one of securityOrigin, storageKey, or storageBucket must be specified.
         * Security origin.
         */
        public val securityOrigin: String? = null,
        /**
         * Storage key.
         */
        public val storageKey: String? = null,
        /**
         * Storage bucket. If not specified, it uses the default bucket.
         */
        public val storageBucket: Storage.StorageBucket? = null,
        /**
         * Database name.
         */
        public val databaseName: String,
    )

    @Serializable
    public data class DeleteObjectStoreEntriesParameter(
        /**
         * At least and at most one of securityOrigin, storageKey, or storageBucket must be specified.
         * Security origin.
         */
        public val securityOrigin: String? = null,
        /**
         * Storage key.
         */
        public val storageKey: String? = null,
        /**
         * Storage bucket. If not specified, it uses the default bucket.
         */
        public val storageBucket: Storage.StorageBucket? = null,
        public val databaseName: String,
        public val objectStoreName: String,
        /**
         * Range of entry keys to delete
         */
        public val keyRange: KeyRange,
    )

    @Serializable
    public data class RequestDataParameter(
        /**
         * At least and at most one of securityOrigin, storageKey, or storageBucket must be specified.
         * Security origin.
         */
        public val securityOrigin: String? = null,
        /**
         * Storage key.
         */
        public val storageKey: String? = null,
        /**
         * Storage bucket. If not specified, it uses the default bucket.
         */
        public val storageBucket: Storage.StorageBucket? = null,
        /**
         * Database name.
         */
        public val databaseName: String,
        /**
         * Object store name.
         */
        public val objectStoreName: String,
        /**
         * Index name, empty string for object store data requests.
         */
        public val indexName: String,
        /**
         * Number of records to skip.
         */
        public val skipCount: Int,
        /**
         * Number of records to fetch.
         */
        public val pageSize: Int,
        /**
         * Key range.
         */
        public val keyRange: KeyRange? = null,
    )

    @Serializable
    public data class RequestDataReturn(
        /**
         * Array of object store data entries.
         */
        public val objectStoreDataEntries: List<DataEntry>,
        /**
         * If true, there are more entries to fetch in the given range.
         */
        public val hasMore: Boolean,
    )

    @Serializable
    public data class GetMetadataParameter(
        /**
         * At least and at most one of securityOrigin, storageKey, or storageBucket must be specified.
         * Security origin.
         */
        public val securityOrigin: String? = null,
        /**
         * Storage key.
         */
        public val storageKey: String? = null,
        /**
         * Storage bucket. If not specified, it uses the default bucket.
         */
        public val storageBucket: Storage.StorageBucket? = null,
        /**
         * Database name.
         */
        public val databaseName: String,
        /**
         * Object store name.
         */
        public val objectStoreName: String,
    )

    @Serializable
    public data class GetMetadataReturn(
        /**
         * the entries count
         */
        public val entriesCount: Double,
        /**
         * the current value of key generator, to become the next inserted
         * key into the object store. Valid if objectStore.autoIncrement
         * is true.
         */
        public val keyGeneratorValue: Double,
    )

    @Serializable
    public data class RequestDatabaseParameter(
        /**
         * At least and at most one of securityOrigin, storageKey, or storageBucket must be specified.
         * Security origin.
         */
        public val securityOrigin: String? = null,
        /**
         * Storage key.
         */
        public val storageKey: String? = null,
        /**
         * Storage bucket. If not specified, it uses the default bucket.
         */
        public val storageBucket: Storage.StorageBucket? = null,
        /**
         * Database name.
         */
        public val databaseName: String,
    )

    @Serializable
    public data class RequestDatabaseReturn(
        /**
         * Database with an array of object stores.
         */
        public val databaseWithObjectStores: DatabaseWithObjectStores,
    )

    @Serializable
    public data class RequestDatabaseNamesParameter(
        /**
         * At least and at most one of securityOrigin, storageKey, or storageBucket must be specified.
         * Security origin.
         */
        public val securityOrigin: String? = null,
        /**
         * Storage key.
         */
        public val storageKey: String? = null,
        /**
         * Storage bucket. If not specified, it uses the default bucket.
         */
        public val storageBucket: Storage.StorageBucket? = null,
    )

    @Serializable
    public data class RequestDatabaseNamesReturn(
        /**
         * Database names for origin.
         */
        public val databaseNames: List<String>,
    )
}
