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
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.encodeToJsonElement

public val CDP.storage: Storage
    get() = getGeneratedDomain() ?: cacheGeneratedDomain(Storage(this))

public class Storage(
    private val cdp: CDP,
) : Domain {
    /**
     * A cache's contents have been modified.
     */
    public val cacheStorageContentUpdated: Flow<CacheStorageContentUpdatedParameter> = cdp
        .events
        .filter { it.method == "Storage.cacheStorageContentUpdated" }
        .map { it.params }
        .filterNotNull()
        .map { Serialization.json.decodeFromJsonElement(it) }

    /**
     * A cache has been added/deleted.
     */
    public val cacheStorageListUpdated: Flow<CacheStorageListUpdatedParameter> = cdp
        .events
        .filter { it.method == "Storage.cacheStorageListUpdated" }
        .map { it.params }
        .filterNotNull()
        .map { Serialization.json.decodeFromJsonElement(it) }

    /**
     * The origin's IndexedDB object store has been modified.
     */
    public val indexedDBContentUpdated: Flow<IndexedDBContentUpdatedParameter> = cdp
        .events
        .filter { it.method == "Storage.indexedDBContentUpdated" }
        .map { it.params }
        .filterNotNull()
        .map { Serialization.json.decodeFromJsonElement(it) }

    /**
     * The origin's IndexedDB database list has been modified.
     */
    public val indexedDBListUpdated: Flow<IndexedDBListUpdatedParameter> = cdp
        .events
        .filter { it.method == "Storage.indexedDBListUpdated" }
        .map { it.params }
        .filterNotNull()
        .map { Serialization.json.decodeFromJsonElement(it) }

    /**
     * One of the interest groups was accessed by the associated page.
     */
    public val interestGroupAccessed: Flow<InterestGroupAccessedParameter> = cdp
        .events
        .filter { it.method == "Storage.interestGroupAccessed" }
        .map { it.params }
        .filterNotNull()
        .map { Serialization.json.decodeFromJsonElement(it) }

    /**
     * Shared storage was accessed by the associated page.
     * The following parameters are included in all events.
     */
    public val sharedStorageAccessed: Flow<SharedStorageAccessedParameter> = cdp
        .events
        .filter { it.method == "Storage.sharedStorageAccessed" }
        .map { it.params }
        .filterNotNull()
        .map { Serialization.json.decodeFromJsonElement(it) }

    public val storageBucketCreatedOrUpdated: Flow<StorageBucketCreatedOrUpdatedParameter> = cdp
        .events
        .filter { it.method == "Storage.storageBucketCreatedOrUpdated" }
        .map { it.params }
        .filterNotNull()
        .map { Serialization.json.decodeFromJsonElement(it) }

    public val storageBucketDeleted: Flow<StorageBucketDeletedParameter> = cdp
        .events
        .filter { it.method == "Storage.storageBucketDeleted" }
        .map { it.params }
        .filterNotNull()
        .map { Serialization.json.decodeFromJsonElement(it) }

    public val attributionReportingSourceRegistered:
            Flow<AttributionReportingSourceRegisteredParameter> = cdp
        .events
        .filter { it.method == "Storage.attributionReportingSourceRegistered" }
        .map { it.params }
        .filterNotNull()
        .map { Serialization.json.decodeFromJsonElement(it) }

    public val attributionReportingTriggerRegistered:
            Flow<AttributionReportingTriggerRegisteredParameter> = cdp
        .events
        .filter { it.method == "Storage.attributionReportingTriggerRegistered" }
        .map { it.params }
        .filterNotNull()
        .map { Serialization.json.decodeFromJsonElement(it) }

    /**
     * Returns a storage key given a frame id.
     */
    public suspend fun getStorageKeyForFrame(args: GetStorageKeyForFrameParameter): GetStorageKeyForFrameReturn {
        val parameter = Serialization.json.encodeToJsonElement(args)
        val result = cdp.callCommand("Storage.getStorageKeyForFrame", parameter)
        return result!!.let { Serialization.json.decodeFromJsonElement(it) }
    }

    /**
     * Returns a storage key given a frame id.
     */
    public suspend fun getStorageKeyForFrame(frameId: String): GetStorageKeyForFrameReturn {
        val parameter = GetStorageKeyForFrameParameter(frameId = frameId)
        return getStorageKeyForFrame(parameter)
    }

    /**
     * Clears storage for origin.
     */
    public suspend fun clearDataForOrigin(args: ClearDataForOriginParameter) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("Storage.clearDataForOrigin", parameter)
    }

    /**
     * Clears storage for origin.
     */
    public suspend fun clearDataForOrigin(origin: String, storageTypes: String) {
        val parameter = ClearDataForOriginParameter(origin = origin, storageTypes = storageTypes)
        clearDataForOrigin(parameter)
    }

    /**
     * Clears storage for storage key.
     */
    public suspend fun clearDataForStorageKey(args: ClearDataForStorageKeyParameter) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("Storage.clearDataForStorageKey", parameter)
    }

    /**
     * Clears storage for storage key.
     */
    public suspend fun clearDataForStorageKey(storageKey: String, storageTypes: String) {
        val parameter = ClearDataForStorageKeyParameter(storageKey = storageKey, storageTypes = storageTypes)
        clearDataForStorageKey(parameter)
    }

    /**
     * Returns all browser cookies.
     */
    public suspend fun getCookies(args: GetCookiesParameter): GetCookiesReturn {
        val parameter = Serialization.json.encodeToJsonElement(args)
        val result = cdp.callCommand("Storage.getCookies", parameter)
        return result!!.let { Serialization.json.decodeFromJsonElement(it) }
    }

    /**
     * Returns all browser cookies.
     */
    public suspend fun getCookies(browserContextId: String? = null): GetCookiesReturn {
        val parameter = GetCookiesParameter(browserContextId = browserContextId)
        return getCookies(parameter)
    }

    /**
     * Sets given cookies.
     */
    public suspend fun setCookies(args: SetCookiesParameter) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("Storage.setCookies", parameter)
    }

    /**
     * Sets given cookies.
     */
    public suspend fun setCookies(cookies: List<Network.CookieParam>, browserContextId: String? = null) {
        val parameter = SetCookiesParameter(cookies = cookies, browserContextId = browserContextId)
        setCookies(parameter)
    }

    /**
     * Clears cookies.
     */
    public suspend fun clearCookies(args: ClearCookiesParameter) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("Storage.clearCookies", parameter)
    }

    /**
     * Clears cookies.
     */
    public suspend fun clearCookies(browserContextId: String? = null) {
        val parameter = ClearCookiesParameter(browserContextId = browserContextId)
        clearCookies(parameter)
    }

    /**
     * Returns usage and quota in bytes.
     */
    public suspend fun getUsageAndQuota(args: GetUsageAndQuotaParameter): GetUsageAndQuotaReturn {
        val parameter = Serialization.json.encodeToJsonElement(args)
        val result = cdp.callCommand("Storage.getUsageAndQuota", parameter)
        return result!!.let { Serialization.json.decodeFromJsonElement(it) }
    }

    /**
     * Returns usage and quota in bytes.
     */
    public suspend fun getUsageAndQuota(origin: String): GetUsageAndQuotaReturn {
        val parameter = GetUsageAndQuotaParameter(origin = origin)
        return getUsageAndQuota(parameter)
    }

    /**
     * Override quota for the specified origin
     */
    public suspend fun overrideQuotaForOrigin(args: OverrideQuotaForOriginParameter) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("Storage.overrideQuotaForOrigin", parameter)
    }

    /**
     * Override quota for the specified origin
     */
    public suspend fun overrideQuotaForOrigin(origin: String, quotaSize: Double? = null) {
        val parameter = OverrideQuotaForOriginParameter(origin = origin, quotaSize = quotaSize)
        overrideQuotaForOrigin(parameter)
    }

    /**
     * Registers origin to be notified when an update occurs to its cache storage list.
     */
    public suspend fun trackCacheStorageForOrigin(args: TrackCacheStorageForOriginParameter) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("Storage.trackCacheStorageForOrigin", parameter)
    }

    /**
     * Registers origin to be notified when an update occurs to its cache storage list.
     */
    public suspend fun trackCacheStorageForOrigin(origin: String) {
        val parameter = TrackCacheStorageForOriginParameter(origin = origin)
        trackCacheStorageForOrigin(parameter)
    }

    /**
     * Registers storage key to be notified when an update occurs to its cache storage list.
     */
    public suspend fun trackCacheStorageForStorageKey(args: TrackCacheStorageForStorageKeyParameter) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("Storage.trackCacheStorageForStorageKey", parameter)
    }

    /**
     * Registers storage key to be notified when an update occurs to its cache storage list.
     */
    public suspend fun trackCacheStorageForStorageKey(storageKey: String) {
        val parameter = TrackCacheStorageForStorageKeyParameter(storageKey = storageKey)
        trackCacheStorageForStorageKey(parameter)
    }

    /**
     * Registers origin to be notified when an update occurs to its IndexedDB.
     */
    public suspend fun trackIndexedDBForOrigin(args: TrackIndexedDBForOriginParameter) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("Storage.trackIndexedDBForOrigin", parameter)
    }

    /**
     * Registers origin to be notified when an update occurs to its IndexedDB.
     */
    public suspend fun trackIndexedDBForOrigin(origin: String) {
        val parameter = TrackIndexedDBForOriginParameter(origin = origin)
        trackIndexedDBForOrigin(parameter)
    }

    /**
     * Registers storage key to be notified when an update occurs to its IndexedDB.
     */
    public suspend fun trackIndexedDBForStorageKey(args: TrackIndexedDBForStorageKeyParameter) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("Storage.trackIndexedDBForStorageKey", parameter)
    }

    /**
     * Registers storage key to be notified when an update occurs to its IndexedDB.
     */
    public suspend fun trackIndexedDBForStorageKey(storageKey: String) {
        val parameter = TrackIndexedDBForStorageKeyParameter(storageKey = storageKey)
        trackIndexedDBForStorageKey(parameter)
    }

    /**
     * Unregisters origin from receiving notifications for cache storage.
     */
    public suspend fun untrackCacheStorageForOrigin(args: UntrackCacheStorageForOriginParameter) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("Storage.untrackCacheStorageForOrigin", parameter)
    }

    /**
     * Unregisters origin from receiving notifications for cache storage.
     */
    public suspend fun untrackCacheStorageForOrigin(origin: String) {
        val parameter = UntrackCacheStorageForOriginParameter(origin = origin)
        untrackCacheStorageForOrigin(parameter)
    }

    /**
     * Unregisters storage key from receiving notifications for cache storage.
     */
    public suspend fun untrackCacheStorageForStorageKey(args: UntrackCacheStorageForStorageKeyParameter) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("Storage.untrackCacheStorageForStorageKey", parameter)
    }

    /**
     * Unregisters storage key from receiving notifications for cache storage.
     */
    public suspend fun untrackCacheStorageForStorageKey(storageKey: String) {
        val parameter = UntrackCacheStorageForStorageKeyParameter(storageKey = storageKey)
        untrackCacheStorageForStorageKey(parameter)
    }

    /**
     * Unregisters origin from receiving notifications for IndexedDB.
     */
    public suspend fun untrackIndexedDBForOrigin(args: UntrackIndexedDBForOriginParameter) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("Storage.untrackIndexedDBForOrigin", parameter)
    }

    /**
     * Unregisters origin from receiving notifications for IndexedDB.
     */
    public suspend fun untrackIndexedDBForOrigin(origin: String) {
        val parameter = UntrackIndexedDBForOriginParameter(origin = origin)
        untrackIndexedDBForOrigin(parameter)
    }

    /**
     * Unregisters storage key from receiving notifications for IndexedDB.
     */
    public suspend fun untrackIndexedDBForStorageKey(args: UntrackIndexedDBForStorageKeyParameter) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("Storage.untrackIndexedDBForStorageKey", parameter)
    }

    /**
     * Unregisters storage key from receiving notifications for IndexedDB.
     */
    public suspend fun untrackIndexedDBForStorageKey(storageKey: String) {
        val parameter = UntrackIndexedDBForStorageKeyParameter(storageKey = storageKey)
        untrackIndexedDBForStorageKey(parameter)
    }

    /**
     * Returns the number of stored Trust Tokens per issuer for the
     * current browsing context.
     */
    public suspend fun getTrustTokens(): GetTrustTokensReturn {
        val parameter = null
        val result = cdp.callCommand("Storage.getTrustTokens", parameter)
        return result!!.let { Serialization.json.decodeFromJsonElement(it) }
    }

    /**
     * Removes all Trust Tokens issued by the provided issuerOrigin.
     * Leaves other stored data, including the issuer's Redemption Records, intact.
     */
    public suspend fun clearTrustTokens(args: ClearTrustTokensParameter): ClearTrustTokensReturn {
        val parameter = Serialization.json.encodeToJsonElement(args)
        val result = cdp.callCommand("Storage.clearTrustTokens", parameter)
        return result!!.let { Serialization.json.decodeFromJsonElement(it) }
    }

    /**
     * Removes all Trust Tokens issued by the provided issuerOrigin.
     * Leaves other stored data, including the issuer's Redemption Records, intact.
     */
    public suspend fun clearTrustTokens(issuerOrigin: String): ClearTrustTokensReturn {
        val parameter = ClearTrustTokensParameter(issuerOrigin = issuerOrigin)
        return clearTrustTokens(parameter)
    }

    /**
     * Gets details for a named interest group.
     */
    public suspend fun getInterestGroupDetails(args: GetInterestGroupDetailsParameter): GetInterestGroupDetailsReturn {
        val parameter = Serialization.json.encodeToJsonElement(args)
        val result = cdp.callCommand("Storage.getInterestGroupDetails", parameter)
        return result!!.let { Serialization.json.decodeFromJsonElement(it) }
    }

    /**
     * Gets details for a named interest group.
     */
    public suspend fun getInterestGroupDetails(ownerOrigin: String, name: String): GetInterestGroupDetailsReturn {
        val parameter = GetInterestGroupDetailsParameter(ownerOrigin = ownerOrigin, name = name)
        return getInterestGroupDetails(parameter)
    }

    /**
     * Enables/Disables issuing of interestGroupAccessed events.
     */
    public suspend fun setInterestGroupTracking(args: SetInterestGroupTrackingParameter) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("Storage.setInterestGroupTracking", parameter)
    }

    /**
     * Enables/Disables issuing of interestGroupAccessed events.
     */
    public suspend fun setInterestGroupTracking(enable: Boolean) {
        val parameter = SetInterestGroupTrackingParameter(enable = enable)
        setInterestGroupTracking(parameter)
    }

    /**
     * Gets metadata for an origin's shared storage.
     */
    public suspend fun getSharedStorageMetadata(args: GetSharedStorageMetadataParameter): GetSharedStorageMetadataReturn {
        val parameter = Serialization.json.encodeToJsonElement(args)
        val result = cdp.callCommand("Storage.getSharedStorageMetadata", parameter)
        return result!!.let { Serialization.json.decodeFromJsonElement(it) }
    }

    /**
     * Gets metadata for an origin's shared storage.
     */
    public suspend fun getSharedStorageMetadata(ownerOrigin: String): GetSharedStorageMetadataReturn {
        val parameter = GetSharedStorageMetadataParameter(ownerOrigin = ownerOrigin)
        return getSharedStorageMetadata(parameter)
    }

    /**
     * Gets the entries in an given origin's shared storage.
     */
    public suspend fun getSharedStorageEntries(args: GetSharedStorageEntriesParameter): GetSharedStorageEntriesReturn {
        val parameter = Serialization.json.encodeToJsonElement(args)
        val result = cdp.callCommand("Storage.getSharedStorageEntries", parameter)
        return result!!.let { Serialization.json.decodeFromJsonElement(it) }
    }

    /**
     * Gets the entries in an given origin's shared storage.
     */
    public suspend fun getSharedStorageEntries(ownerOrigin: String): GetSharedStorageEntriesReturn {
        val parameter = GetSharedStorageEntriesParameter(ownerOrigin = ownerOrigin)
        return getSharedStorageEntries(parameter)
    }

    /**
     * Sets entry with `key` and `value` for a given origin's shared storage.
     */
    public suspend fun setSharedStorageEntry(args: SetSharedStorageEntryParameter) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("Storage.setSharedStorageEntry", parameter)
    }

    /**
     * Sets entry with `key` and `value` for a given origin's shared storage.
     */
    public suspend fun setSharedStorageEntry(
        ownerOrigin: String,
        key: String,
        `value`: String,
        ignoreIfPresent: Boolean? = null,
    ) {
        val parameter = SetSharedStorageEntryParameter(
            ownerOrigin = ownerOrigin,
            key = key,
            value = value,
            ignoreIfPresent = ignoreIfPresent
        )
        setSharedStorageEntry(parameter)
    }

    /**
     * Deletes entry for `key` (if it exists) for a given origin's shared storage.
     */
    public suspend fun deleteSharedStorageEntry(args: DeleteSharedStorageEntryParameter) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("Storage.deleteSharedStorageEntry", parameter)
    }

    /**
     * Deletes entry for `key` (if it exists) for a given origin's shared storage.
     */
    public suspend fun deleteSharedStorageEntry(ownerOrigin: String, key: String) {
        val parameter = DeleteSharedStorageEntryParameter(ownerOrigin = ownerOrigin, key = key)
        deleteSharedStorageEntry(parameter)
    }

    /**
     * Clears all entries for a given origin's shared storage.
     */
    public suspend fun clearSharedStorageEntries(args: ClearSharedStorageEntriesParameter) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("Storage.clearSharedStorageEntries", parameter)
    }

    /**
     * Clears all entries for a given origin's shared storage.
     */
    public suspend fun clearSharedStorageEntries(ownerOrigin: String) {
        val parameter = ClearSharedStorageEntriesParameter(ownerOrigin = ownerOrigin)
        clearSharedStorageEntries(parameter)
    }

    /**
     * Resets the budget for `ownerOrigin` by clearing all budget withdrawals.
     */
    public suspend fun resetSharedStorageBudget(args: ResetSharedStorageBudgetParameter) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("Storage.resetSharedStorageBudget", parameter)
    }

    /**
     * Resets the budget for `ownerOrigin` by clearing all budget withdrawals.
     */
    public suspend fun resetSharedStorageBudget(ownerOrigin: String) {
        val parameter = ResetSharedStorageBudgetParameter(ownerOrigin = ownerOrigin)
        resetSharedStorageBudget(parameter)
    }

    /**
     * Enables/disables issuing of sharedStorageAccessed events.
     */
    public suspend fun setSharedStorageTracking(args: SetSharedStorageTrackingParameter) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("Storage.setSharedStorageTracking", parameter)
    }

    /**
     * Enables/disables issuing of sharedStorageAccessed events.
     */
    public suspend fun setSharedStorageTracking(enable: Boolean) {
        val parameter = SetSharedStorageTrackingParameter(enable = enable)
        setSharedStorageTracking(parameter)
    }

    /**
     * Set tracking for a storage key's buckets.
     */
    public suspend fun setStorageBucketTracking(args: SetStorageBucketTrackingParameter) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("Storage.setStorageBucketTracking", parameter)
    }

    /**
     * Set tracking for a storage key's buckets.
     */
    public suspend fun setStorageBucketTracking(storageKey: String, enable: Boolean) {
        val parameter = SetStorageBucketTrackingParameter(storageKey = storageKey, enable = enable)
        setStorageBucketTracking(parameter)
    }

    /**
     * Deletes the Storage Bucket with the given storage key and bucket name.
     */
    public suspend fun deleteStorageBucket(args: DeleteStorageBucketParameter) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("Storage.deleteStorageBucket", parameter)
    }

    /**
     * Deletes the Storage Bucket with the given storage key and bucket name.
     */
    public suspend fun deleteStorageBucket(bucket: StorageBucket) {
        val parameter = DeleteStorageBucketParameter(bucket = bucket)
        deleteStorageBucket(parameter)
    }

    /**
     * Deletes state for sites identified as potential bounce trackers, immediately.
     */
    public suspend fun runBounceTrackingMitigations(): RunBounceTrackingMitigationsReturn {
        val parameter = null
        val result = cdp.callCommand("Storage.runBounceTrackingMitigations", parameter)
        return result!!.let { Serialization.json.decodeFromJsonElement(it) }
    }

    /**
     * https://wicg.github.io/attribution-reporting-api/
     */
    public suspend fun setAttributionReportingLocalTestingMode(args: SetAttributionReportingLocalTestingModeParameter) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("Storage.setAttributionReportingLocalTestingMode", parameter)
    }

    /**
     * https://wicg.github.io/attribution-reporting-api/
     */
    public suspend fun setAttributionReportingLocalTestingMode(enabled: Boolean) {
        val parameter = SetAttributionReportingLocalTestingModeParameter(enabled = enabled)
        setAttributionReportingLocalTestingMode(parameter)
    }

    /**
     * Enables/disables issuing of Attribution Reporting events.
     */
    public suspend fun setAttributionReportingTracking(args: SetAttributionReportingTrackingParameter) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("Storage.setAttributionReportingTracking", parameter)
    }

    /**
     * Enables/disables issuing of Attribution Reporting events.
     */
    public suspend fun setAttributionReportingTracking(enable: Boolean) {
        val parameter = SetAttributionReportingTrackingParameter(enable = enable)
        setAttributionReportingTracking(parameter)
    }

    /**
     * Enum of possible storage types.
     */
    @Serializable
    public enum class StorageType {
        @SerialName("appcache")
        APPCACHE,

        @SerialName("cookies")
        COOKIES,

        @SerialName("file_systems")
        FILE_SYSTEMS,

        @SerialName("indexeddb")
        INDEXEDDB,

        @SerialName("local_storage")
        LOCAL_STORAGE,

        @SerialName("shader_cache")
        SHADER_CACHE,

        @SerialName("websql")
        WEBSQL,

        @SerialName("service_workers")
        SERVICE_WORKERS,

        @SerialName("cache_storage")
        CACHE_STORAGE,

        @SerialName("interest_groups")
        INTEREST_GROUPS,

        @SerialName("shared_storage")
        SHARED_STORAGE,

        @SerialName("storage_buckets")
        STORAGE_BUCKETS,

        @SerialName("all")
        ALL,

        @SerialName("other")
        OTHER,
    }

    /**
     * Usage for a storage type.
     */
    @Serializable
    public data class UsageForType(
        /**
         * Name of storage type.
         */
        public val storageType: StorageType,
        /**
         * Storage usage (bytes).
         */
        public val usage: Double,
    )

    /**
     * Pair of issuer origin and number of available (signed, but not used) Trust
     * Tokens from that issuer.
     */
    @Serializable
    public data class TrustTokens(
        public val issuerOrigin: String,
        public val count: Double,
    )

    /**
     * Enum of interest group access types.
     */
    @Serializable
    public enum class InterestGroupAccessType {
        @SerialName("join")
        JOIN,

        @SerialName("leave")
        LEAVE,

        @SerialName("update")
        UPDATE,

        @SerialName("loaded")
        LOADED,

        @SerialName("bid")
        BID,

        @SerialName("win")
        WIN,

        @SerialName("additionalBid")
        ADDITIONALBID,

        @SerialName("additionalBidWin")
        ADDITIONALBIDWIN,

        @SerialName("clear")
        CLEAR,
    }

    /**
     * Ad advertising element inside an interest group.
     */
    @Serializable
    public data class InterestGroupAd(
        public val renderURL: String,
        public val metadata: String? = null,
    )

    /**
     * The full details of an interest group.
     */
    @Serializable
    public data class InterestGroupDetails(
        public val ownerOrigin: String,
        public val name: String,
        public val expirationTime: Double,
        public val joiningOrigin: String,
        public val biddingLogicURL: String? = null,
        public val biddingWasmHelperURL: String? = null,
        public val updateURL: String? = null,
        public val trustedBiddingSignalsURL: String? = null,
        public val trustedBiddingSignalsKeys: List<String>,
        public val userBiddingSignals: String? = null,
        public val ads: List<InterestGroupAd>,
        public val adComponents: List<InterestGroupAd>,
    )

    /**
     * Enum of shared storage access types.
     */
    @Serializable
    public enum class SharedStorageAccessType {
        @SerialName("documentAddModule")
        DOCUMENTADDMODULE,

        @SerialName("documentSelectURL")
        DOCUMENTSELECTURL,

        @SerialName("documentRun")
        DOCUMENTRUN,

        @SerialName("documentSet")
        DOCUMENTSET,

        @SerialName("documentAppend")
        DOCUMENTAPPEND,

        @SerialName("documentDelete")
        DOCUMENTDELETE,

        @SerialName("documentClear")
        DOCUMENTCLEAR,

        @SerialName("workletSet")
        WORKLETSET,

        @SerialName("workletAppend")
        WORKLETAPPEND,

        @SerialName("workletDelete")
        WORKLETDELETE,

        @SerialName("workletClear")
        WORKLETCLEAR,

        @SerialName("workletGet")
        WORKLETGET,

        @SerialName("workletKeys")
        WORKLETKEYS,

        @SerialName("workletEntries")
        WORKLETENTRIES,

        @SerialName("workletLength")
        WORKLETLENGTH,

        @SerialName("workletRemainingBudget")
        WORKLETREMAININGBUDGET,
    }

    /**
     * Struct for a single key-value pair in an origin's shared storage.
     */
    @Serializable
    public data class SharedStorageEntry(
        public val key: String,
        public val `value`: String,
    )

    /**
     * Details for an origin's shared storage.
     */
    @Serializable
    public data class SharedStorageMetadata(
        public val creationTime: Double,
        public val length: Int,
        public val remainingBudget: Double,
    )

    /**
     * Pair of reporting metadata details for a candidate URL for `selectURL()`.
     */
    @Serializable
    public data class SharedStorageReportingMetadata(
        public val eventType: String,
        public val reportingUrl: String,
    )

    /**
     * Bundles a candidate URL with its reporting metadata.
     */
    @Serializable
    public data class SharedStorageUrlWithMetadata(
        /**
         * Spec of candidate URL.
         */
        public val url: String,
        /**
         * Any associated reporting metadata.
         */
        public val reportingMetadata: List<SharedStorageReportingMetadata>,
    )

    /**
     * Bundles the parameters for shared storage access events whose
     * presence/absence can vary according to SharedStorageAccessType.
     */
    @Serializable
    public data class SharedStorageAccessParams(
        /**
         * Spec of the module script URL.
         * Present only for SharedStorageAccessType.documentAddModule.
         */
        public val scriptSourceUrl: String? = null,
        /**
         * Name of the registered operation to be run.
         * Present only for SharedStorageAccessType.documentRun and
         * SharedStorageAccessType.documentSelectURL.
         */
        public val operationName: String? = null,
        /**
         * The operation's serialized data in bytes (converted to a string).
         * Present only for SharedStorageAccessType.documentRun and
         * SharedStorageAccessType.documentSelectURL.
         */
        public val serializedData: String? = null,
        /**
         * Array of candidate URLs' specs, along with any associated metadata.
         * Present only for SharedStorageAccessType.documentSelectURL.
         */
        public val urlsWithMetadata: List<SharedStorageUrlWithMetadata>? = null,
        /**
         * Key for a specific entry in an origin's shared storage.
         * Present only for SharedStorageAccessType.documentSet,
         * SharedStorageAccessType.documentAppend,
         * SharedStorageAccessType.documentDelete,
         * SharedStorageAccessType.workletSet,
         * SharedStorageAccessType.workletAppend,
         * SharedStorageAccessType.workletDelete, and
         * SharedStorageAccessType.workletGet.
         */
        public val key: String? = null,
        /**
         * Value for a specific entry in an origin's shared storage.
         * Present only for SharedStorageAccessType.documentSet,
         * SharedStorageAccessType.documentAppend,
         * SharedStorageAccessType.workletSet, and
         * SharedStorageAccessType.workletAppend.
         */
        public val `value`: String? = null,
        /**
         * Whether or not to set an entry for a key if that key is already present.
         * Present only for SharedStorageAccessType.documentSet and
         * SharedStorageAccessType.workletSet.
         */
        public val ignoreIfPresent: Boolean? = null,
    )

    @Serializable
    public enum class StorageBucketsDurability {
        @SerialName("relaxed")
        RELAXED,

        @SerialName("strict")
        STRICT,
    }

    @Serializable
    public data class StorageBucket(
        public val storageKey: String,
        /**
         * If not specified, it is the default bucket of the storageKey.
         */
        public val name: String? = null,
    )

    @Serializable
    public data class StorageBucketInfo(
        public val bucket: StorageBucket,
        public val id: String,
        public val expiration: Double,
        /**
         * Storage quota (bytes).
         */
        public val quota: Double,
        public val persistent: Boolean,
        public val durability: StorageBucketsDurability,
    )

    @Serializable
    public enum class AttributionReportingSourceType {
        @SerialName("navigation")
        NAVIGATION,

        @SerialName("event")
        EVENT,
    }

    @Serializable
    public data class AttributionReportingFilterDataEntry(
        public val key: String,
        public val values: List<String>,
    )

    @Serializable
    public data class AttributionReportingFilterConfig(
        public val filterValues: List<AttributionReportingFilterDataEntry>,
        /**
         * duration in seconds
         */
        public val lookbackWindow: Int? = null,
    )

    @Serializable
    public data class AttributionReportingFilterPair(
        public val filters: List<AttributionReportingFilterConfig>,
        public val notFilters: List<AttributionReportingFilterConfig>,
    )

    @Serializable
    public data class AttributionReportingAggregationKeysEntry(
        public val key: String,
        public val `value`: String,
    )

    @Serializable
    public data class AttributionReportingEventReportWindows(
        /**
         * duration in seconds
         */
        public val start: Int,
        /**
         * duration in seconds
         */
        public val ends: List<Int>,
    )

    @Serializable
    public data class AttributionReportingTriggerSpec(
        /**
         * number instead of integer because not all uint32 can be represented by
         * int
         */
        public val triggerData: List<Double>,
        public val eventReportWindows: AttributionReportingEventReportWindows,
    )

    @Serializable
    public enum class AttributionReportingTriggerDataMatching {
        @SerialName("exact")
        EXACT,

        @SerialName("modulus")
        MODULUS,
    }

    @Serializable
    public data class AttributionReportingSourceRegistration(
        public val time: Double,
        /**
         * duration in seconds
         */
        public val expiry: Int,
        public val triggerSpecs: List<AttributionReportingTriggerSpec>,
        /**
         * duration in seconds
         */
        public val aggregatableReportWindow: Int,
        public val type: AttributionReportingSourceType,
        public val sourceOrigin: String,
        public val reportingOrigin: String,
        public val destinationSites: List<String>,
        public val eventId: String,
        public val priority: String,
        public val filterData: List<AttributionReportingFilterDataEntry>,
        public val aggregationKeys: List<AttributionReportingAggregationKeysEntry>,
        public val debugKey: String? = null,
        public val triggerDataMatching: AttributionReportingTriggerDataMatching,
    )

    @Serializable
    public enum class AttributionReportingSourceRegistrationResult {
        @SerialName("success")
        SUCCESS,

        @SerialName("internalError")
        INTERNALERROR,

        @SerialName("insufficientSourceCapacity")
        INSUFFICIENTSOURCECAPACITY,

        @SerialName("insufficientUniqueDestinationCapacity")
        INSUFFICIENTUNIQUEDESTINATIONCAPACITY,

        @SerialName("excessiveReportingOrigins")
        EXCESSIVEREPORTINGORIGINS,

        @SerialName("prohibitedByBrowserPolicy")
        PROHIBITEDBYBROWSERPOLICY,

        @SerialName("successNoised")
        SUCCESSNOISED,

        @SerialName("destinationReportingLimitReached")
        DESTINATIONREPORTINGLIMITREACHED,

        @SerialName("destinationGlobalLimitReached")
        DESTINATIONGLOBALLIMITREACHED,

        @SerialName("destinationBothLimitsReached")
        DESTINATIONBOTHLIMITSREACHED,

        @SerialName("reportingOriginsPerSiteLimitReached")
        REPORTINGORIGINSPERSITELIMITREACHED,

        @SerialName("exceedsMaxChannelCapacity")
        EXCEEDSMAXCHANNELCAPACITY,
    }

    @Serializable
    public enum class AttributionReportingSourceRegistrationTimeConfig {
        @SerialName("include")
        INCLUDE,

        @SerialName("exclude")
        EXCLUDE,
    }

    @Serializable
    public data class AttributionReportingAggregatableValueEntry(
        public val key: String,
        /**
         * number instead of integer because not all uint32 can be represented by
         * int
         */
        public val `value`: Double,
    )

    @Serializable
    public data class AttributionReportingEventTriggerData(
        public val `data`: String,
        public val priority: String,
        public val dedupKey: String? = null,
        public val filters: AttributionReportingFilterPair,
    )

    @Serializable
    public data class AttributionReportingAggregatableTriggerData(
        public val keyPiece: String,
        public val sourceKeys: List<String>,
        public val filters: AttributionReportingFilterPair,
    )

    @Serializable
    public data class AttributionReportingAggregatableDedupKey(
        public val dedupKey: String? = null,
        public val filters: AttributionReportingFilterPair,
    )

    @Serializable
    public data class AttributionReportingTriggerRegistration(
        public val filters: AttributionReportingFilterPair,
        public val debugKey: String? = null,
        public val aggregatableDedupKeys: List<AttributionReportingAggregatableDedupKey>,
        public val eventTriggerData: List<AttributionReportingEventTriggerData>,
        public val aggregatableTriggerData: List<AttributionReportingAggregatableTriggerData>,
        public val aggregatableValues: List<AttributionReportingAggregatableValueEntry>,
        public val debugReporting: Boolean,
        public val aggregationCoordinatorOrigin: String? = null,
        public val sourceRegistrationTimeConfig: AttributionReportingSourceRegistrationTimeConfig,
        public val triggerContextId: String? = null,
    )

    @Serializable
    public enum class AttributionReportingEventLevelResult {
        @SerialName("success")
        SUCCESS,

        @SerialName("successDroppedLowerPriority")
        SUCCESSDROPPEDLOWERPRIORITY,

        @SerialName("internalError")
        INTERNALERROR,

        @SerialName("noCapacityForAttributionDestination")
        NOCAPACITYFORATTRIBUTIONDESTINATION,

        @SerialName("noMatchingSources")
        NOMATCHINGSOURCES,

        @SerialName("deduplicated")
        DEDUPLICATED,

        @SerialName("excessiveAttributions")
        EXCESSIVEATTRIBUTIONS,

        @SerialName("priorityTooLow")
        PRIORITYTOOLOW,

        @SerialName("neverAttributedSource")
        NEVERATTRIBUTEDSOURCE,

        @SerialName("excessiveReportingOrigins")
        EXCESSIVEREPORTINGORIGINS,

        @SerialName("noMatchingSourceFilterData")
        NOMATCHINGSOURCEFILTERDATA,

        @SerialName("prohibitedByBrowserPolicy")
        PROHIBITEDBYBROWSERPOLICY,

        @SerialName("noMatchingConfigurations")
        NOMATCHINGCONFIGURATIONS,

        @SerialName("excessiveReports")
        EXCESSIVEREPORTS,

        @SerialName("falselyAttributedSource")
        FALSELYATTRIBUTEDSOURCE,

        @SerialName("reportWindowPassed")
        REPORTWINDOWPASSED,

        @SerialName("notRegistered")
        NOTREGISTERED,

        @SerialName("reportWindowNotStarted")
        REPORTWINDOWNOTSTARTED,

        @SerialName("noMatchingTriggerData")
        NOMATCHINGTRIGGERDATA,
    }

    @Serializable
    public enum class AttributionReportingAggregatableResult {
        @SerialName("success")
        SUCCESS,

        @SerialName("internalError")
        INTERNALERROR,

        @SerialName("noCapacityForAttributionDestination")
        NOCAPACITYFORATTRIBUTIONDESTINATION,

        @SerialName("noMatchingSources")
        NOMATCHINGSOURCES,

        @SerialName("excessiveAttributions")
        EXCESSIVEATTRIBUTIONS,

        @SerialName("excessiveReportingOrigins")
        EXCESSIVEREPORTINGORIGINS,

        @SerialName("noHistograms")
        NOHISTOGRAMS,

        @SerialName("insufficientBudget")
        INSUFFICIENTBUDGET,

        @SerialName("noMatchingSourceFilterData")
        NOMATCHINGSOURCEFILTERDATA,

        @SerialName("notRegistered")
        NOTREGISTERED,

        @SerialName("prohibitedByBrowserPolicy")
        PROHIBITEDBYBROWSERPOLICY,

        @SerialName("deduplicated")
        DEDUPLICATED,

        @SerialName("reportWindowPassed")
        REPORTWINDOWPASSED,

        @SerialName("excessiveReports")
        EXCESSIVEREPORTS,
    }

    /**
     * A cache's contents have been modified.
     */
    @Serializable
    public data class CacheStorageContentUpdatedParameter(
        /**
         * Origin to update.
         */
        public val origin: String,
        /**
         * Storage key to update.
         */
        public val storageKey: String,
        /**
         * Storage bucket to update.
         */
        public val bucketId: String,
        /**
         * Name of cache in origin.
         */
        public val cacheName: String,
    )

    /**
     * A cache has been added/deleted.
     */
    @Serializable
    public data class CacheStorageListUpdatedParameter(
        /**
         * Origin to update.
         */
        public val origin: String,
        /**
         * Storage key to update.
         */
        public val storageKey: String,
        /**
         * Storage bucket to update.
         */
        public val bucketId: String,
    )

    /**
     * The origin's IndexedDB object store has been modified.
     */
    @Serializable
    public data class IndexedDBContentUpdatedParameter(
        /**
         * Origin to update.
         */
        public val origin: String,
        /**
         * Storage key to update.
         */
        public val storageKey: String,
        /**
         * Storage bucket to update.
         */
        public val bucketId: String,
        /**
         * Database to update.
         */
        public val databaseName: String,
        /**
         * ObjectStore to update.
         */
        public val objectStoreName: String,
    )

    /**
     * The origin's IndexedDB database list has been modified.
     */
    @Serializable
    public data class IndexedDBListUpdatedParameter(
        /**
         * Origin to update.
         */
        public val origin: String,
        /**
         * Storage key to update.
         */
        public val storageKey: String,
        /**
         * Storage bucket to update.
         */
        public val bucketId: String,
    )

    /**
     * One of the interest groups was accessed by the associated page.
     */
    @Serializable
    public data class InterestGroupAccessedParameter(
        public val accessTime: Double,
        public val type: InterestGroupAccessType,
        public val ownerOrigin: String,
        public val name: String,
    )

    /**
     * Shared storage was accessed by the associated page.
     * The following parameters are included in all events.
     */
    @Serializable
    public data class SharedStorageAccessedParameter(
        /**
         * Time of the access.
         */
        public val accessTime: Double,
        /**
         * Enum value indicating the Shared Storage API method invoked.
         */
        public val type: SharedStorageAccessType,
        /**
         * DevTools Frame Token for the primary frame tree's root.
         */
        public val mainFrameId: String,
        /**
         * Serialized origin for the context that invoked the Shared Storage API.
         */
        public val ownerOrigin: String,
        /**
         * The sub-parameters warapped by `params` are all optional and their
         * presence/absence depends on `type`.
         */
        public val params: SharedStorageAccessParams,
    )

    @Serializable
    public data class StorageBucketCreatedOrUpdatedParameter(
        public val bucketInfo: StorageBucketInfo,
    )

    @Serializable
    public data class StorageBucketDeletedParameter(
        public val bucketId: String,
    )

    @Serializable
    public data class AttributionReportingSourceRegisteredParameter(
        public val registration: AttributionReportingSourceRegistration,
        public val result: AttributionReportingSourceRegistrationResult,
    )

    @Serializable
    public data class AttributionReportingTriggerRegisteredParameter(
        public val registration: AttributionReportingTriggerRegistration,
        public val eventLevel: AttributionReportingEventLevelResult,
        public val aggregatable: AttributionReportingAggregatableResult,
    )

    @Serializable
    public data class GetStorageKeyForFrameParameter(
        public val frameId: String,
    )

    @Serializable
    public data class GetStorageKeyForFrameReturn(
        public val storageKey: String,
    )

    @Serializable
    public data class ClearDataForOriginParameter(
        /**
         * Security origin.
         */
        public val origin: String,
        /**
         * Comma separated list of StorageType to clear.
         */
        public val storageTypes: String,
    )

    @Serializable
    public data class ClearDataForStorageKeyParameter(
        /**
         * Storage key.
         */
        public val storageKey: String,
        /**
         * Comma separated list of StorageType to clear.
         */
        public val storageTypes: String,
    )

    @Serializable
    public data class GetCookiesParameter(
        /**
         * Browser context to use when called on the browser endpoint.
         */
        public val browserContextId: String? = null,
    )

    @Serializable
    public data class GetCookiesReturn(
        /**
         * Array of cookie objects.
         */
        public val cookies: List<Network.Cookie>,
    )

    @Serializable
    public data class SetCookiesParameter(
        /**
         * Cookies to be set.
         */
        public val cookies: List<Network.CookieParam>,
        /**
         * Browser context to use when called on the browser endpoint.
         */
        public val browserContextId: String? = null,
    )

    @Serializable
    public data class ClearCookiesParameter(
        /**
         * Browser context to use when called on the browser endpoint.
         */
        public val browserContextId: String? = null,
    )

    @Serializable
    public data class GetUsageAndQuotaParameter(
        /**
         * Security origin.
         */
        public val origin: String,
    )

    @Serializable
    public data class GetUsageAndQuotaReturn(
        /**
         * Storage usage (bytes).
         */
        public val usage: Double,
        /**
         * Storage quota (bytes).
         */
        public val quota: Double,
        /**
         * Whether or not the origin has an active storage quota override
         */
        public val overrideActive: Boolean,
        /**
         * Storage usage per type (bytes).
         */
        public val usageBreakdown: List<UsageForType>,
    )

    @Serializable
    public data class OverrideQuotaForOriginParameter(
        /**
         * Security origin.
         */
        public val origin: String,
        /**
         * The quota size (in bytes) to override the original quota with.
         * If this is called multiple times, the overridden quota will be equal to
         * the quotaSize provided in the final call. If this is called without
         * specifying a quotaSize, the quota will be reset to the default value for
         * the specified origin. If this is called multiple times with different
         * origins, the override will be maintained for each origin until it is
         * disabled (called without a quotaSize).
         */
        public val quotaSize: Double? = null,
    )

    @Serializable
    public data class TrackCacheStorageForOriginParameter(
        /**
         * Security origin.
         */
        public val origin: String,
    )

    @Serializable
    public data class TrackCacheStorageForStorageKeyParameter(
        /**
         * Storage key.
         */
        public val storageKey: String,
    )

    @Serializable
    public data class TrackIndexedDBForOriginParameter(
        /**
         * Security origin.
         */
        public val origin: String,
    )

    @Serializable
    public data class TrackIndexedDBForStorageKeyParameter(
        /**
         * Storage key.
         */
        public val storageKey: String,
    )

    @Serializable
    public data class UntrackCacheStorageForOriginParameter(
        /**
         * Security origin.
         */
        public val origin: String,
    )

    @Serializable
    public data class UntrackCacheStorageForStorageKeyParameter(
        /**
         * Storage key.
         */
        public val storageKey: String,
    )

    @Serializable
    public data class UntrackIndexedDBForOriginParameter(
        /**
         * Security origin.
         */
        public val origin: String,
    )

    @Serializable
    public data class UntrackIndexedDBForStorageKeyParameter(
        /**
         * Storage key.
         */
        public val storageKey: String,
    )

    @Serializable
    public data class GetTrustTokensReturn(
        public val tokens: List<TrustTokens>,
    )

    @Serializable
    public data class ClearTrustTokensParameter(
        public val issuerOrigin: String,
    )

    @Serializable
    public data class ClearTrustTokensReturn(
        /**
         * True if any tokens were deleted, false otherwise.
         */
        public val didDeleteTokens: Boolean,
    )

    @Serializable
    public data class GetInterestGroupDetailsParameter(
        public val ownerOrigin: String,
        public val name: String,
    )

    @Serializable
    public data class GetInterestGroupDetailsReturn(
        public val details: InterestGroupDetails,
    )

    @Serializable
    public data class SetInterestGroupTrackingParameter(
        public val enable: Boolean,
    )

    @Serializable
    public data class GetSharedStorageMetadataParameter(
        public val ownerOrigin: String,
    )

    @Serializable
    public data class GetSharedStorageMetadataReturn(
        public val metadata: SharedStorageMetadata,
    )

    @Serializable
    public data class GetSharedStorageEntriesParameter(
        public val ownerOrigin: String,
    )

    @Serializable
    public data class GetSharedStorageEntriesReturn(
        public val entries: List<SharedStorageEntry>,
    )

    @Serializable
    public data class SetSharedStorageEntryParameter(
        public val ownerOrigin: String,
        public val key: String,
        public val `value`: String,
        /**
         * If `ignoreIfPresent` is included and true, then only sets the entry if
         * `key` doesn't already exist.
         */
        public val ignoreIfPresent: Boolean? = null,
    )

    @Serializable
    public data class DeleteSharedStorageEntryParameter(
        public val ownerOrigin: String,
        public val key: String,
    )

    @Serializable
    public data class ClearSharedStorageEntriesParameter(
        public val ownerOrigin: String,
    )

    @Serializable
    public data class ResetSharedStorageBudgetParameter(
        public val ownerOrigin: String,
    )

    @Serializable
    public data class SetSharedStorageTrackingParameter(
        public val enable: Boolean,
    )

    @Serializable
    public data class SetStorageBucketTrackingParameter(
        public val storageKey: String,
        public val enable: Boolean,
    )

    @Serializable
    public data class DeleteStorageBucketParameter(
        public val bucket: StorageBucket,
    )

    @Serializable
    public data class RunBounceTrackingMitigationsReturn(
        public val deletedSites: List<String>,
    )

    @Serializable
    public data class SetAttributionReportingLocalTestingModeParameter(
        /**
         * If enabled, noise is suppressed and reports are sent immediately.
         */
        public val enabled: Boolean,
    )

    @Serializable
    public data class SetAttributionReportingTrackingParameter(
        public val enable: Boolean,
    )
}
