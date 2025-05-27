package dev.kdriver.cdp.domain

import dev.kaccelero.serializers.Serialization
import dev.kdriver.cdp.CDP
import dev.kdriver.cdp.Domain
import dev.kdriver.cdp.cacheGeneratedDomain
import dev.kdriver.cdp.getGeneratedDomain
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.encodeToJsonElement

public val CDP.cacheStorage: CacheStorage
    get() = getGeneratedDomain() ?: cacheGeneratedDomain(CacheStorage(this))

public class CacheStorage(
    private val cdp: CDP,
) : Domain {
    /**
     * Deletes a cache.
     */
    public suspend fun deleteCache(args: DeleteCacheParameter) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("CacheStorage.deleteCache", parameter)
    }

    /**
     * Deletes a cache.
     */
    public suspend fun deleteCache(cacheId: String) {
        val parameter = DeleteCacheParameter(cacheId = cacheId)
        deleteCache(parameter)
    }

    /**
     * Deletes a cache entry.
     */
    public suspend fun deleteEntry(args: DeleteEntryParameter) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("CacheStorage.deleteEntry", parameter)
    }

    /**
     * Deletes a cache entry.
     */
    public suspend fun deleteEntry(cacheId: String, request: String) {
        val parameter = DeleteEntryParameter(cacheId = cacheId, request = request)
        deleteEntry(parameter)
    }

    /**
     * Requests cache names.
     */
    public suspend fun requestCacheNames(args: RequestCacheNamesParameter): RequestCacheNamesReturn {
        val parameter = Serialization.json.encodeToJsonElement(args)
        val result = cdp.callCommand("CacheStorage.requestCacheNames", parameter)
        return result!!.let { Serialization.json.decodeFromJsonElement(it) }
    }

    /**
     * Requests cache names.
     */
    public suspend fun requestCacheNames(
        securityOrigin: String? = null,
        storageKey: String? = null,
        storageBucket: Storage.StorageBucket? = null,
    ): RequestCacheNamesReturn {
        val parameter = RequestCacheNamesParameter(
            securityOrigin = securityOrigin,
            storageKey = storageKey,
            storageBucket = storageBucket
        )
        return requestCacheNames(parameter)
    }

    /**
     * Fetches cache entry.
     */
    public suspend fun requestCachedResponse(args: RequestCachedResponseParameter): RequestCachedResponseReturn {
        val parameter = Serialization.json.encodeToJsonElement(args)
        val result = cdp.callCommand("CacheStorage.requestCachedResponse", parameter)
        return result!!.let { Serialization.json.decodeFromJsonElement(it) }
    }

    /**
     * Fetches cache entry.
     */
    public suspend fun requestCachedResponse(
        cacheId: String,
        requestURL: String,
        requestHeaders: List<Header>,
    ): RequestCachedResponseReturn {
        val parameter =
            RequestCachedResponseParameter(cacheId = cacheId, requestURL = requestURL, requestHeaders = requestHeaders)
        return requestCachedResponse(parameter)
    }

    /**
     * Requests data from cache.
     */
    public suspend fun requestEntries(args: RequestEntriesParameter): RequestEntriesReturn {
        val parameter = Serialization.json.encodeToJsonElement(args)
        val result = cdp.callCommand("CacheStorage.requestEntries", parameter)
        return result!!.let { Serialization.json.decodeFromJsonElement(it) }
    }

    /**
     * Requests data from cache.
     */
    public suspend fun requestEntries(
        cacheId: String,
        skipCount: Int? = null,
        pageSize: Int? = null,
        pathFilter: String? = null,
    ): RequestEntriesReturn {
        val parameter = RequestEntriesParameter(
            cacheId = cacheId,
            skipCount = skipCount,
            pageSize = pageSize,
            pathFilter = pathFilter
        )
        return requestEntries(parameter)
    }

    /**
     * type of HTTP response cached
     */
    @Serializable
    public enum class CachedResponseType {
        @SerialName("basic")
        BASIC,

        @SerialName("cors")
        CORS,

        @SerialName("default")
        DEFAULT,

        @SerialName("error")
        ERROR,

        @SerialName("opaqueResponse")
        OPAQUERESPONSE,

        @SerialName("opaqueRedirect")
        OPAQUEREDIRECT,
    }

    /**
     * Data entry.
     */
    @Serializable
    public data class DataEntry(
        /**
         * Request URL.
         */
        public val requestURL: String,
        /**
         * Request method.
         */
        public val requestMethod: String,
        /**
         * Request headers
         */
        public val requestHeaders: List<Header>,
        /**
         * Number of seconds since epoch.
         */
        public val responseTime: Double,
        /**
         * HTTP response status code.
         */
        public val responseStatus: Int,
        /**
         * HTTP response status text.
         */
        public val responseStatusText: String,
        /**
         * HTTP response type
         */
        public val responseType: CachedResponseType,
        /**
         * Response headers
         */
        public val responseHeaders: List<Header>,
    )

    /**
     * Cache identifier.
     */
    @Serializable
    public data class Cache(
        /**
         * An opaque unique id of the cache.
         */
        public val cacheId: String,
        /**
         * Security origin of the cache.
         */
        public val securityOrigin: String,
        /**
         * Storage key of the cache.
         */
        public val storageKey: String,
        /**
         * Storage bucket of the cache.
         */
        public val storageBucket: Storage.StorageBucket? = null,
        /**
         * The name of the cache.
         */
        public val cacheName: String,
    )

    @Serializable
    public data class Header(
        public val name: String,
        public val `value`: String,
    )

    /**
     * Cached response
     */
    @Serializable
    public data class CachedResponse(
        /**
         * Entry content, base64-encoded. (Encoded as a base64 string when passed over JSON)
         */
        public val body: String,
    )

    @Serializable
    public data class DeleteCacheParameter(
        /**
         * Id of cache for deletion.
         */
        public val cacheId: String,
    )

    @Serializable
    public data class DeleteEntryParameter(
        /**
         * Id of cache where the entry will be deleted.
         */
        public val cacheId: String,
        /**
         * URL spec of the request.
         */
        public val request: String,
    )

    @Serializable
    public data class RequestCacheNamesParameter(
        /**
         * At least and at most one of securityOrigin, storageKey, storageBucket must be specified.
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
    public data class RequestCacheNamesReturn(
        /**
         * Caches for the security origin.
         */
        public val caches: List<Cache>,
    )

    @Serializable
    public data class RequestCachedResponseParameter(
        /**
         * Id of cache that contains the entry.
         */
        public val cacheId: String,
        /**
         * URL spec of the request.
         */
        public val requestURL: String,
        /**
         * headers of the request.
         */
        public val requestHeaders: List<Header>,
    )

    @Serializable
    public data class RequestCachedResponseReturn(
        /**
         * Response read from the cache.
         */
        public val response: CachedResponse,
    )

    @Serializable
    public data class RequestEntriesParameter(
        /**
         * ID of cache to get entries from.
         */
        public val cacheId: String,
        /**
         * Number of records to skip.
         */
        public val skipCount: Int? = null,
        /**
         * Number of records to fetch.
         */
        public val pageSize: Int? = null,
        /**
         * If present, only return the entries containing this substring in the path
         */
        public val pathFilter: String? = null,
    )

    @Serializable
    public data class RequestEntriesReturn(
        /**
         * Array of object store data entries.
         */
        public val cacheDataEntries: List<DataEntry>,
        /**
         * Count of returned entries from this storage. If pathFilter is empty, it
         * is the count of all entries from this storage.
         */
        public val returnCount: Double,
    )
}
