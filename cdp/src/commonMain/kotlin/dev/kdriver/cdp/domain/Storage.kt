package dev.kdriver.cdp.domain

import dev.kaccelero.serializers.Serialization
import dev.kdriver.cdp.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
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
     * One of the interest groups was accessed. Note that these events are global
     * to all targets sharing an interest group store.
     */
    public val interestGroupAccessed: Flow<InterestGroupAccessedParameter> = cdp
        .events
        .filter { it.method == "Storage.interestGroupAccessed" }
        .map { it.params }
        .filterNotNull()
        .map { Serialization.json.decodeFromJsonElement(it) }

    /**
     * An auction involving interest groups is taking place. These events are
     * target-specific.
     */
    public val interestGroupAuctionEventOccurred: Flow<InterestGroupAuctionEventOccurredParameter> =
        cdp
            .events
            .filter { it.method == "Storage.interestGroupAuctionEventOccurred" }
            .map { it.params }
            .filterNotNull()
            .map { Serialization.json.decodeFromJsonElement(it) }

    /**
     * Specifies which auctions a particular network fetch may be related to, and
     * in what role. Note that it is not ordered with respect to
     * Network.requestWillBeSent (but will happen before loadingFinished
     * loadingFailed).
     */
    public val interestGroupAuctionNetworkRequestCreated:
            Flow<InterestGroupAuctionNetworkRequestCreatedParameter> = cdp
        .events
        .filter { it.method == "Storage.interestGroupAuctionNetworkRequestCreated" }
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

    /**
     * A shared storage run or selectURL operation finished its execution.
     * The following parameters are included in all events.
     */
    public val sharedStorageWorkletOperationExecutionFinished:
            Flow<SharedStorageWorkletOperationExecutionFinishedParameter> = cdp
        .events
        .filter { it.method == "Storage.sharedStorageWorkletOperationExecutionFinished" }
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

    public val attributionReportingReportSent: Flow<AttributionReportingReportSentParameter> = cdp
        .events
        .filter { it.method == "Storage.attributionReportingReportSent" }
        .map { it.params }
        .filterNotNull()
        .map { Serialization.json.decodeFromJsonElement(it) }

    public val attributionReportingVerboseDebugReportSent:
            Flow<AttributionReportingVerboseDebugReportSentParameter> = cdp
        .events
        .filter { it.method == "Storage.attributionReportingVerboseDebugReportSent" }
        .map { it.params }
        .filterNotNull()
        .map { Serialization.json.decodeFromJsonElement(it) }

    /**
     * Returns a storage key given a frame id.
     */
    public suspend fun getStorageKeyForFrame(
        args: GetStorageKeyForFrameParameter,
        mode: CommandMode = CommandMode.DEFAULT,
    ): GetStorageKeyForFrameReturn {
        val parameter = Serialization.json.encodeToJsonElement(args)
        val result = cdp.callCommand("Storage.getStorageKeyForFrame", parameter, mode)
        return result!!.let { Serialization.json.decodeFromJsonElement(it) }
    }

    /**
     * Returns a storage key given a frame id.
     *
     * @param frameId No description
     */
    public suspend fun getStorageKeyForFrame(frameId: String): GetStorageKeyForFrameReturn {
        val parameter = GetStorageKeyForFrameParameter(frameId = frameId)
        return getStorageKeyForFrame(parameter)
    }

    /**
     * Clears storage for origin.
     */
    public suspend fun clearDataForOrigin(args: ClearDataForOriginParameter, mode: CommandMode = CommandMode.DEFAULT) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("Storage.clearDataForOrigin", parameter, mode)
    }

    /**
     * Clears storage for origin.
     *
     * @param origin Security origin.
     * @param storageTypes Comma separated list of StorageType to clear.
     */
    public suspend fun clearDataForOrigin(origin: String, storageTypes: String) {
        val parameter = ClearDataForOriginParameter(origin = origin, storageTypes = storageTypes)
        clearDataForOrigin(parameter)
    }

    /**
     * Clears storage for storage key.
     */
    public suspend fun clearDataForStorageKey(
        args: ClearDataForStorageKeyParameter,
        mode: CommandMode = CommandMode.DEFAULT,
    ) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("Storage.clearDataForStorageKey", parameter, mode)
    }

    /**
     * Clears storage for storage key.
     *
     * @param storageKey Storage key.
     * @param storageTypes Comma separated list of StorageType to clear.
     */
    public suspend fun clearDataForStorageKey(storageKey: String, storageTypes: String) {
        val parameter = ClearDataForStorageKeyParameter(storageKey = storageKey, storageTypes = storageTypes)
        clearDataForStorageKey(parameter)
    }

    /**
     * Returns all browser cookies.
     */
    public suspend fun getCookies(
        args: GetCookiesParameter,
        mode: CommandMode = CommandMode.DEFAULT,
    ): GetCookiesReturn {
        val parameter = Serialization.json.encodeToJsonElement(args)
        val result = cdp.callCommand("Storage.getCookies", parameter, mode)
        return result!!.let { Serialization.json.decodeFromJsonElement(it) }
    }

    /**
     * Returns all browser cookies.
     *
     * @param browserContextId Browser context to use when called on the browser endpoint.
     */
    public suspend fun getCookies(browserContextId: String? = null): GetCookiesReturn {
        val parameter = GetCookiesParameter(browserContextId = browserContextId)
        return getCookies(parameter)
    }

    /**
     * Sets given cookies.
     */
    public suspend fun setCookies(args: SetCookiesParameter, mode: CommandMode = CommandMode.DEFAULT) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("Storage.setCookies", parameter, mode)
    }

    /**
     * Sets given cookies.
     *
     * @param cookies Cookies to be set.
     * @param browserContextId Browser context to use when called on the browser endpoint.
     */
    public suspend fun setCookies(cookies: List<Network.CookieParam>, browserContextId: String? = null) {
        val parameter = SetCookiesParameter(cookies = cookies, browserContextId = browserContextId)
        setCookies(parameter)
    }

    /**
     * Clears cookies.
     */
    public suspend fun clearCookies(args: ClearCookiesParameter, mode: CommandMode = CommandMode.DEFAULT) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("Storage.clearCookies", parameter, mode)
    }

    /**
     * Clears cookies.
     *
     * @param browserContextId Browser context to use when called on the browser endpoint.
     */
    public suspend fun clearCookies(browserContextId: String? = null) {
        val parameter = ClearCookiesParameter(browserContextId = browserContextId)
        clearCookies(parameter)
    }

    /**
     * Returns usage and quota in bytes.
     */
    public suspend fun getUsageAndQuota(
        args: GetUsageAndQuotaParameter,
        mode: CommandMode = CommandMode.DEFAULT,
    ): GetUsageAndQuotaReturn {
        val parameter = Serialization.json.encodeToJsonElement(args)
        val result = cdp.callCommand("Storage.getUsageAndQuota", parameter, mode)
        return result!!.let { Serialization.json.decodeFromJsonElement(it) }
    }

    /**
     * Returns usage and quota in bytes.
     *
     * @param origin Security origin.
     */
    public suspend fun getUsageAndQuota(origin: String): GetUsageAndQuotaReturn {
        val parameter = GetUsageAndQuotaParameter(origin = origin)
        return getUsageAndQuota(parameter)
    }

    /**
     * Override quota for the specified origin
     */
    public suspend fun overrideQuotaForOrigin(
        args: OverrideQuotaForOriginParameter,
        mode: CommandMode = CommandMode.DEFAULT,
    ) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("Storage.overrideQuotaForOrigin", parameter, mode)
    }

    /**
     * Override quota for the specified origin
     *
     * @param origin Security origin.
     * @param quotaSize The quota size (in bytes) to override the original quota with.
     * If this is called multiple times, the overridden quota will be equal to
     * the quotaSize provided in the final call. If this is called without
     * specifying a quotaSize, the quota will be reset to the default value for
     * the specified origin. If this is called multiple times with different
     * origins, the override will be maintained for each origin until it is
     * disabled (called without a quotaSize).
     */
    public suspend fun overrideQuotaForOrigin(origin: String, quotaSize: Double? = null) {
        val parameter = OverrideQuotaForOriginParameter(origin = origin, quotaSize = quotaSize)
        overrideQuotaForOrigin(parameter)
    }

    /**
     * Registers origin to be notified when an update occurs to its cache storage list.
     */
    public suspend fun trackCacheStorageForOrigin(
        args: TrackCacheStorageForOriginParameter,
        mode: CommandMode = CommandMode.DEFAULT,
    ) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("Storage.trackCacheStorageForOrigin", parameter, mode)
    }

    /**
     * Registers origin to be notified when an update occurs to its cache storage list.
     *
     * @param origin Security origin.
     */
    public suspend fun trackCacheStorageForOrigin(origin: String) {
        val parameter = TrackCacheStorageForOriginParameter(origin = origin)
        trackCacheStorageForOrigin(parameter)
    }

    /**
     * Registers storage key to be notified when an update occurs to its cache storage list.
     */
    public suspend fun trackCacheStorageForStorageKey(
        args: TrackCacheStorageForStorageKeyParameter,
        mode: CommandMode = CommandMode.DEFAULT,
    ) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("Storage.trackCacheStorageForStorageKey", parameter, mode)
    }

    /**
     * Registers storage key to be notified when an update occurs to its cache storage list.
     *
     * @param storageKey Storage key.
     */
    public suspend fun trackCacheStorageForStorageKey(storageKey: String) {
        val parameter = TrackCacheStorageForStorageKeyParameter(storageKey = storageKey)
        trackCacheStorageForStorageKey(parameter)
    }

    /**
     * Registers origin to be notified when an update occurs to its IndexedDB.
     */
    public suspend fun trackIndexedDBForOrigin(
        args: TrackIndexedDBForOriginParameter,
        mode: CommandMode = CommandMode.DEFAULT,
    ) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("Storage.trackIndexedDBForOrigin", parameter, mode)
    }

    /**
     * Registers origin to be notified when an update occurs to its IndexedDB.
     *
     * @param origin Security origin.
     */
    public suspend fun trackIndexedDBForOrigin(origin: String) {
        val parameter = TrackIndexedDBForOriginParameter(origin = origin)
        trackIndexedDBForOrigin(parameter)
    }

    /**
     * Registers storage key to be notified when an update occurs to its IndexedDB.
     */
    public suspend fun trackIndexedDBForStorageKey(
        args: TrackIndexedDBForStorageKeyParameter,
        mode: CommandMode = CommandMode.DEFAULT,
    ) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("Storage.trackIndexedDBForStorageKey", parameter, mode)
    }

    /**
     * Registers storage key to be notified when an update occurs to its IndexedDB.
     *
     * @param storageKey Storage key.
     */
    public suspend fun trackIndexedDBForStorageKey(storageKey: String) {
        val parameter = TrackIndexedDBForStorageKeyParameter(storageKey = storageKey)
        trackIndexedDBForStorageKey(parameter)
    }

    /**
     * Unregisters origin from receiving notifications for cache storage.
     */
    public suspend fun untrackCacheStorageForOrigin(
        args: UntrackCacheStorageForOriginParameter,
        mode: CommandMode = CommandMode.DEFAULT,
    ) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("Storage.untrackCacheStorageForOrigin", parameter, mode)
    }

    /**
     * Unregisters origin from receiving notifications for cache storage.
     *
     * @param origin Security origin.
     */
    public suspend fun untrackCacheStorageForOrigin(origin: String) {
        val parameter = UntrackCacheStorageForOriginParameter(origin = origin)
        untrackCacheStorageForOrigin(parameter)
    }

    /**
     * Unregisters storage key from receiving notifications for cache storage.
     */
    public suspend fun untrackCacheStorageForStorageKey(
        args: UntrackCacheStorageForStorageKeyParameter,
        mode: CommandMode = CommandMode.DEFAULT,
    ) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("Storage.untrackCacheStorageForStorageKey", parameter, mode)
    }

    /**
     * Unregisters storage key from receiving notifications for cache storage.
     *
     * @param storageKey Storage key.
     */
    public suspend fun untrackCacheStorageForStorageKey(storageKey: String) {
        val parameter = UntrackCacheStorageForStorageKeyParameter(storageKey = storageKey)
        untrackCacheStorageForStorageKey(parameter)
    }

    /**
     * Unregisters origin from receiving notifications for IndexedDB.
     */
    public suspend fun untrackIndexedDBForOrigin(
        args: UntrackIndexedDBForOriginParameter,
        mode: CommandMode = CommandMode.DEFAULT,
    ) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("Storage.untrackIndexedDBForOrigin", parameter, mode)
    }

    /**
     * Unregisters origin from receiving notifications for IndexedDB.
     *
     * @param origin Security origin.
     */
    public suspend fun untrackIndexedDBForOrigin(origin: String) {
        val parameter = UntrackIndexedDBForOriginParameter(origin = origin)
        untrackIndexedDBForOrigin(parameter)
    }

    /**
     * Unregisters storage key from receiving notifications for IndexedDB.
     */
    public suspend fun untrackIndexedDBForStorageKey(
        args: UntrackIndexedDBForStorageKeyParameter,
        mode: CommandMode = CommandMode.DEFAULT,
    ) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("Storage.untrackIndexedDBForStorageKey", parameter, mode)
    }

    /**
     * Unregisters storage key from receiving notifications for IndexedDB.
     *
     * @param storageKey Storage key.
     */
    public suspend fun untrackIndexedDBForStorageKey(storageKey: String) {
        val parameter = UntrackIndexedDBForStorageKeyParameter(storageKey = storageKey)
        untrackIndexedDBForStorageKey(parameter)
    }

    /**
     * Returns the number of stored Trust Tokens per issuer for the
     * current browsing context.
     */
    public suspend fun getTrustTokens(mode: CommandMode = CommandMode.DEFAULT): GetTrustTokensReturn {
        val parameter = null
        val result = cdp.callCommand("Storage.getTrustTokens", parameter, mode)
        return result!!.let { Serialization.json.decodeFromJsonElement(it) }
    }

    /**
     * Removes all Trust Tokens issued by the provided issuerOrigin.
     * Leaves other stored data, including the issuer's Redemption Records, intact.
     */
    public suspend fun clearTrustTokens(
        args: ClearTrustTokensParameter,
        mode: CommandMode = CommandMode.DEFAULT,
    ): ClearTrustTokensReturn {
        val parameter = Serialization.json.encodeToJsonElement(args)
        val result = cdp.callCommand("Storage.clearTrustTokens", parameter, mode)
        return result!!.let { Serialization.json.decodeFromJsonElement(it) }
    }

    /**
     * Removes all Trust Tokens issued by the provided issuerOrigin.
     * Leaves other stored data, including the issuer's Redemption Records, intact.
     *
     * @param issuerOrigin No description
     */
    public suspend fun clearTrustTokens(issuerOrigin: String): ClearTrustTokensReturn {
        val parameter = ClearTrustTokensParameter(issuerOrigin = issuerOrigin)
        return clearTrustTokens(parameter)
    }

    /**
     * Gets details for a named interest group.
     */
    public suspend fun getInterestGroupDetails(
        args: GetInterestGroupDetailsParameter,
        mode: CommandMode = CommandMode.DEFAULT,
    ): GetInterestGroupDetailsReturn {
        val parameter = Serialization.json.encodeToJsonElement(args)
        val result = cdp.callCommand("Storage.getInterestGroupDetails", parameter, mode)
        return result!!.let { Serialization.json.decodeFromJsonElement(it) }
    }

    /**
     * Gets details for a named interest group.
     *
     * @param ownerOrigin No description
     * @param name No description
     */
    public suspend fun getInterestGroupDetails(ownerOrigin: String, name: String): GetInterestGroupDetailsReturn {
        val parameter = GetInterestGroupDetailsParameter(ownerOrigin = ownerOrigin, name = name)
        return getInterestGroupDetails(parameter)
    }

    /**
     * Enables/Disables issuing of interestGroupAccessed events.
     */
    public suspend fun setInterestGroupTracking(
        args: SetInterestGroupTrackingParameter,
        mode: CommandMode = CommandMode.DEFAULT,
    ) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("Storage.setInterestGroupTracking", parameter, mode)
    }

    /**
     * Enables/Disables issuing of interestGroupAccessed events.
     *
     * @param enable No description
     */
    public suspend fun setInterestGroupTracking(enable: Boolean) {
        val parameter = SetInterestGroupTrackingParameter(enable = enable)
        setInterestGroupTracking(parameter)
    }

    /**
     * Enables/Disables issuing of interestGroupAuctionEventOccurred and
     * interestGroupAuctionNetworkRequestCreated.
     */
    public suspend fun setInterestGroupAuctionTracking(
        args: SetInterestGroupAuctionTrackingParameter,
        mode: CommandMode = CommandMode.DEFAULT,
    ) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("Storage.setInterestGroupAuctionTracking", parameter, mode)
    }

    /**
     * Enables/Disables issuing of interestGroupAuctionEventOccurred and
     * interestGroupAuctionNetworkRequestCreated.
     *
     * @param enable No description
     */
    public suspend fun setInterestGroupAuctionTracking(enable: Boolean) {
        val parameter = SetInterestGroupAuctionTrackingParameter(enable = enable)
        setInterestGroupAuctionTracking(parameter)
    }

    /**
     * Gets metadata for an origin's shared storage.
     */
    public suspend fun getSharedStorageMetadata(
        args: GetSharedStorageMetadataParameter,
        mode: CommandMode = CommandMode.DEFAULT,
    ): GetSharedStorageMetadataReturn {
        val parameter = Serialization.json.encodeToJsonElement(args)
        val result = cdp.callCommand("Storage.getSharedStorageMetadata", parameter, mode)
        return result!!.let { Serialization.json.decodeFromJsonElement(it) }
    }

    /**
     * Gets metadata for an origin's shared storage.
     *
     * @param ownerOrigin No description
     */
    public suspend fun getSharedStorageMetadata(ownerOrigin: String): GetSharedStorageMetadataReturn {
        val parameter = GetSharedStorageMetadataParameter(ownerOrigin = ownerOrigin)
        return getSharedStorageMetadata(parameter)
    }

    /**
     * Gets the entries in an given origin's shared storage.
     */
    public suspend fun getSharedStorageEntries(
        args: GetSharedStorageEntriesParameter,
        mode: CommandMode = CommandMode.DEFAULT,
    ): GetSharedStorageEntriesReturn {
        val parameter = Serialization.json.encodeToJsonElement(args)
        val result = cdp.callCommand("Storage.getSharedStorageEntries", parameter, mode)
        return result!!.let { Serialization.json.decodeFromJsonElement(it) }
    }

    /**
     * Gets the entries in an given origin's shared storage.
     *
     * @param ownerOrigin No description
     */
    public suspend fun getSharedStorageEntries(ownerOrigin: String): GetSharedStorageEntriesReturn {
        val parameter = GetSharedStorageEntriesParameter(ownerOrigin = ownerOrigin)
        return getSharedStorageEntries(parameter)
    }

    /**
     * Sets entry with `key` and `value` for a given origin's shared storage.
     */
    public suspend fun setSharedStorageEntry(
        args: SetSharedStorageEntryParameter,
        mode: CommandMode = CommandMode.DEFAULT,
    ) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("Storage.setSharedStorageEntry", parameter, mode)
    }

    /**
     * Sets entry with `key` and `value` for a given origin's shared storage.
     *
     * @param ownerOrigin No description
     * @param key No description
     * @param value No description
     * @param ignoreIfPresent If `ignoreIfPresent` is included and true, then only sets the entry if
     * `key` doesn't already exist.
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
    public suspend fun deleteSharedStorageEntry(
        args: DeleteSharedStorageEntryParameter,
        mode: CommandMode = CommandMode.DEFAULT,
    ) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("Storage.deleteSharedStorageEntry", parameter, mode)
    }

    /**
     * Deletes entry for `key` (if it exists) for a given origin's shared storage.
     *
     * @param ownerOrigin No description
     * @param key No description
     */
    public suspend fun deleteSharedStorageEntry(ownerOrigin: String, key: String) {
        val parameter = DeleteSharedStorageEntryParameter(ownerOrigin = ownerOrigin, key = key)
        deleteSharedStorageEntry(parameter)
    }

    /**
     * Clears all entries for a given origin's shared storage.
     */
    public suspend fun clearSharedStorageEntries(
        args: ClearSharedStorageEntriesParameter,
        mode: CommandMode = CommandMode.DEFAULT,
    ) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("Storage.clearSharedStorageEntries", parameter, mode)
    }

    /**
     * Clears all entries for a given origin's shared storage.
     *
     * @param ownerOrigin No description
     */
    public suspend fun clearSharedStorageEntries(ownerOrigin: String) {
        val parameter = ClearSharedStorageEntriesParameter(ownerOrigin = ownerOrigin)
        clearSharedStorageEntries(parameter)
    }

    /**
     * Resets the budget for `ownerOrigin` by clearing all budget withdrawals.
     */
    public suspend fun resetSharedStorageBudget(
        args: ResetSharedStorageBudgetParameter,
        mode: CommandMode = CommandMode.DEFAULT,
    ) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("Storage.resetSharedStorageBudget", parameter, mode)
    }

    /**
     * Resets the budget for `ownerOrigin` by clearing all budget withdrawals.
     *
     * @param ownerOrigin No description
     */
    public suspend fun resetSharedStorageBudget(ownerOrigin: String) {
        val parameter = ResetSharedStorageBudgetParameter(ownerOrigin = ownerOrigin)
        resetSharedStorageBudget(parameter)
    }

    /**
     * Enables/disables issuing of sharedStorageAccessed events.
     */
    public suspend fun setSharedStorageTracking(
        args: SetSharedStorageTrackingParameter,
        mode: CommandMode = CommandMode.DEFAULT,
    ) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("Storage.setSharedStorageTracking", parameter, mode)
    }

    /**
     * Enables/disables issuing of sharedStorageAccessed events.
     *
     * @param enable No description
     */
    public suspend fun setSharedStorageTracking(enable: Boolean) {
        val parameter = SetSharedStorageTrackingParameter(enable = enable)
        setSharedStorageTracking(parameter)
    }

    /**
     * Set tracking for a storage key's buckets.
     */
    public suspend fun setStorageBucketTracking(
        args: SetStorageBucketTrackingParameter,
        mode: CommandMode = CommandMode.DEFAULT,
    ) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("Storage.setStorageBucketTracking", parameter, mode)
    }

    /**
     * Set tracking for a storage key's buckets.
     *
     * @param storageKey No description
     * @param enable No description
     */
    public suspend fun setStorageBucketTracking(storageKey: String, enable: Boolean) {
        val parameter = SetStorageBucketTrackingParameter(storageKey = storageKey, enable = enable)
        setStorageBucketTracking(parameter)
    }

    /**
     * Deletes the Storage Bucket with the given storage key and bucket name.
     */
    public suspend fun deleteStorageBucket(
        args: DeleteStorageBucketParameter,
        mode: CommandMode = CommandMode.DEFAULT,
    ) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("Storage.deleteStorageBucket", parameter, mode)
    }

    /**
     * Deletes the Storage Bucket with the given storage key and bucket name.
     *
     * @param bucket No description
     */
    public suspend fun deleteStorageBucket(bucket: StorageBucket) {
        val parameter = DeleteStorageBucketParameter(bucket = bucket)
        deleteStorageBucket(parameter)
    }

    /**
     * Deletes state for sites identified as potential bounce trackers, immediately.
     */
    public suspend fun runBounceTrackingMitigations(mode: CommandMode = CommandMode.DEFAULT): RunBounceTrackingMitigationsReturn {
        val parameter = null
        val result = cdp.callCommand("Storage.runBounceTrackingMitigations", parameter, mode)
        return result!!.let { Serialization.json.decodeFromJsonElement(it) }
    }

    /**
     * https://wicg.github.io/attribution-reporting-api/
     */
    public suspend fun setAttributionReportingLocalTestingMode(
        args: SetAttributionReportingLocalTestingModeParameter,
        mode: CommandMode = CommandMode.DEFAULT,
    ) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("Storage.setAttributionReportingLocalTestingMode", parameter, mode)
    }

    /**
     * https://wicg.github.io/attribution-reporting-api/
     *
     * @param enabled If enabled, noise is suppressed and reports are sent immediately.
     */
    public suspend fun setAttributionReportingLocalTestingMode(enabled: Boolean) {
        val parameter = SetAttributionReportingLocalTestingModeParameter(enabled = enabled)
        setAttributionReportingLocalTestingMode(parameter)
    }

    /**
     * Enables/disables issuing of Attribution Reporting events.
     */
    public suspend fun setAttributionReportingTracking(
        args: SetAttributionReportingTrackingParameter,
        mode: CommandMode = CommandMode.DEFAULT,
    ) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("Storage.setAttributionReportingTracking", parameter, mode)
    }

    /**
     * Enables/disables issuing of Attribution Reporting events.
     *
     * @param enable No description
     */
    public suspend fun setAttributionReportingTracking(enable: Boolean) {
        val parameter = SetAttributionReportingTrackingParameter(enable = enable)
        setAttributionReportingTracking(parameter)
    }

    /**
     * Sends all pending Attribution Reports immediately, regardless of their
     * scheduled report time.
     */
    public suspend fun sendPendingAttributionReports(mode: CommandMode = CommandMode.DEFAULT): SendPendingAttributionReportsReturn {
        val parameter = null
        val result = cdp.callCommand("Storage.sendPendingAttributionReports", parameter, mode)
        return result!!.let { Serialization.json.decodeFromJsonElement(it) }
    }

    /**
     * Returns the effective Related Website Sets in use by this profile for the browser
     * session. The effective Related Website Sets will not change during a browser session.
     */
    public suspend fun getRelatedWebsiteSets(mode: CommandMode = CommandMode.DEFAULT): GetRelatedWebsiteSetsReturn {
        val parameter = null
        val result = cdp.callCommand("Storage.getRelatedWebsiteSets", parameter, mode)
        return result!!.let { Serialization.json.decodeFromJsonElement(it) }
    }

    /**
     * Returns the list of URLs from a page and its embedded resources that match
     * existing grace period URL pattern rules.
     * https://developers.google.com/privacy-sandbox/cookies/temporary-exceptions/grace-period
     */
    public suspend fun getAffectedUrlsForThirdPartyCookieMetadata(
        args: GetAffectedUrlsForThirdPartyCookieMetadataParameter,
        mode: CommandMode = CommandMode.DEFAULT,
    ): GetAffectedUrlsForThirdPartyCookieMetadataReturn {
        val parameter = Serialization.json.encodeToJsonElement(args)
        val result = cdp.callCommand("Storage.getAffectedUrlsForThirdPartyCookieMetadata", parameter, mode)
        return result!!.let { Serialization.json.decodeFromJsonElement(it) }
    }

    /**
     * Returns the list of URLs from a page and its embedded resources that match
     * existing grace period URL pattern rules.
     * https://developers.google.com/privacy-sandbox/cookies/temporary-exceptions/grace-period
     *
     * @param firstPartyUrl The URL of the page currently being visited.
     * @param thirdPartyUrls The list of embedded resource URLs from the page.
     */
    public suspend fun getAffectedUrlsForThirdPartyCookieMetadata(
        firstPartyUrl: String,
        thirdPartyUrls: List<String>,
    ): GetAffectedUrlsForThirdPartyCookieMetadataReturn {
        val parameter = GetAffectedUrlsForThirdPartyCookieMetadataParameter(
            firstPartyUrl = firstPartyUrl,
            thirdPartyUrls = thirdPartyUrls
        )
        return getAffectedUrlsForThirdPartyCookieMetadata(parameter)
    }

    public suspend fun setProtectedAudienceKAnonymity(
        args: SetProtectedAudienceKAnonymityParameter,
        mode: CommandMode = CommandMode.DEFAULT,
    ) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("Storage.setProtectedAudienceKAnonymity", parameter, mode)
    }

    /**
     *
     *
     * @param owner No description
     * @param name No description
     * @param hashes No description
     */
    public suspend fun setProtectedAudienceKAnonymity(
        owner: String,
        name: String,
        hashes: List<String>,
    ) {
        val parameter = SetProtectedAudienceKAnonymityParameter(owner = owner, name = name, hashes = hashes)
        setProtectedAudienceKAnonymity(parameter)
    }

    /**
     * Enum of possible storage types.
     */
    @Serializable
    public enum class StorageType {
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

        @SerialName("topLevelBid")
        TOPLEVELBID,

        @SerialName("topLevelAdditionalBid")
        TOPLEVELADDITIONALBID,

        @SerialName("clear")
        CLEAR,
    }

    /**
     * Enum of auction events.
     */
    @Serializable
    public enum class InterestGroupAuctionEventType {
        @SerialName("started")
        STARTED,

        @SerialName("configResolved")
        CONFIGRESOLVED,
    }

    /**
     * Enum of network fetches auctions can do.
     */
    @Serializable
    public enum class InterestGroupAuctionFetchType {
        @SerialName("bidderJs")
        BIDDERJS,

        @SerialName("bidderWasm")
        BIDDERWASM,

        @SerialName("sellerJs")
        SELLERJS,

        @SerialName("bidderTrustedSignals")
        BIDDERTRUSTEDSIGNALS,

        @SerialName("sellerTrustedSignals")
        SELLERTRUSTEDSIGNALS,
    }

    /**
     * Enum of shared storage access scopes.
     */
    @Serializable
    public enum class SharedStorageAccessScope {
        @SerialName("window")
        WINDOW,

        @SerialName("sharedStorageWorklet")
        SHAREDSTORAGEWORKLET,

        @SerialName("protectedAudienceWorklet")
        PROTECTEDAUDIENCEWORKLET,

        @SerialName("header")
        HEADER,
    }

    /**
     * Enum of shared storage access methods.
     */
    @Serializable
    public enum class SharedStorageAccessMethod {
        @SerialName("addModule")
        ADDMODULE,

        @SerialName("createWorklet")
        CREATEWORKLET,

        @SerialName("selectURL")
        SELECTURL,

        @SerialName("run")
        RUN,

        @SerialName("batchUpdate")
        BATCHUPDATE,

        @SerialName("set")
        SET,

        @SerialName("append")
        APPEND,

        @SerialName("delete")
        DELETE,

        @SerialName("clear")
        CLEAR,

        @SerialName("get")
        GET,

        @SerialName("keys")
        KEYS,

        @SerialName("values")
        VALUES,

        @SerialName("entries")
        ENTRIES,

        @SerialName("length")
        LENGTH,

        @SerialName("remainingBudget")
        REMAININGBUDGET,
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
        /**
         * Time when the origin's shared storage was last created.
         */
        public val creationTime: Double,
        /**
         * Number of key-value pairs stored in origin's shared storage.
         */
        public val length: Int,
        /**
         * Current amount of bits of entropy remaining in the navigation budget.
         */
        public val remainingBudget: Double,
        /**
         * Total number of bytes stored as key-value pairs in origin's shared
         * storage.
         */
        public val bytesUsed: Int,
    )

    /**
     * Represents a dictionary object passed in as privateAggregationConfig to
     * run or selectURL.
     */
    @Serializable
    public data class SharedStoragePrivateAggregationConfig(
        /**
         * The chosen aggregation service deployment.
         */
        public val aggregationCoordinatorOrigin: String? = null,
        /**
         * The context ID provided.
         */
        public val contextId: String? = null,
        /**
         * Configures the maximum size allowed for filtering IDs.
         */
        public val filteringIdMaxBytes: Int,
        /**
         * The limit on the number of contributions in the final report.
         */
        public val maxContributions: Int? = null,
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
         * Present only for SharedStorageAccessMethods: addModule and
         * createWorklet.
         */
        public val scriptSourceUrl: String? = null,
        /**
         * String denoting "context-origin", "script-origin", or a custom
         * origin to be used as the worklet's data origin.
         * Present only for SharedStorageAccessMethod: createWorklet.
         */
        public val dataOrigin: String? = null,
        /**
         * Name of the registered operation to be run.
         * Present only for SharedStorageAccessMethods: run and selectURL.
         */
        public val operationName: String? = null,
        /**
         * ID of the operation call.
         * Present only for SharedStorageAccessMethods: run and selectURL.
         */
        public val operationId: String? = null,
        /**
         * Whether or not to keep the worket alive for future run or selectURL
         * calls.
         * Present only for SharedStorageAccessMethods: run and selectURL.
         */
        public val keepAlive: Boolean? = null,
        /**
         * Configures the private aggregation options.
         * Present only for SharedStorageAccessMethods: run and selectURL.
         */
        public val privateAggregationConfig: SharedStoragePrivateAggregationConfig? = null,
        /**
         * The operation's serialized data in bytes (converted to a string).
         * Present only for SharedStorageAccessMethods: run and selectURL.
         * TODO(crbug.com/401011862): Consider updating this parameter to binary.
         */
        public val serializedData: String? = null,
        /**
         * Array of candidate URLs' specs, along with any associated metadata.
         * Present only for SharedStorageAccessMethod: selectURL.
         */
        public val urlsWithMetadata: List<SharedStorageUrlWithMetadata>? = null,
        /**
         * Spec of the URN:UUID generated for a selectURL call.
         * Present only for SharedStorageAccessMethod: selectURL.
         */
        public val urnUuid: String? = null,
        /**
         * Key for a specific entry in an origin's shared storage.
         * Present only for SharedStorageAccessMethods: set, append, delete, and
         * get.
         */
        public val key: String? = null,
        /**
         * Value for a specific entry in an origin's shared storage.
         * Present only for SharedStorageAccessMethods: set and append.
         */
        public val `value`: String? = null,
        /**
         * Whether or not to set an entry for a key if that key is already present.
         * Present only for SharedStorageAccessMethod: set.
         */
        public val ignoreIfPresent: Boolean? = null,
        /**
         * A number denoting the (0-based) order of the worklet's
         * creation relative to all other shared storage worklets created by
         * documents using the current storage partition.
         * Present only for SharedStorageAccessMethods: addModule, createWorklet.
         */
        public val workletOrdinal: Int? = null,
        /**
         * Hex representation of the DevTools token used as the TargetID for the
         * associated shared storage worklet.
         * Present only for SharedStorageAccessMethods: addModule, createWorklet,
         * run, selectURL, and any other SharedStorageAccessMethod when the
         * SharedStorageAccessScope is sharedStorageWorklet.
         */
        public val workletTargetId: String? = null,
        /**
         * Name of the lock to be acquired, if present.
         * Optionally present only for SharedStorageAccessMethods: batchUpdate,
         * set, append, delete, and clear.
         */
        public val withLock: String? = null,
        /**
         * If the method has been called as part of a batchUpdate, then this
         * number identifies the batch to which it belongs.
         * Optionally present only for SharedStorageAccessMethods:
         * batchUpdate (required), set, append, delete, and clear.
         */
        public val batchUpdateId: String? = null,
        /**
         * Number of modifier methods sent in batch.
         * Present only for SharedStorageAccessMethod: batchUpdate.
         */
        public val batchSize: Int? = null,
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
    public enum class AttributionReportingTriggerDataMatching {
        @SerialName("exact")
        EXACT,

        @SerialName("modulus")
        MODULUS,
    }

    @Serializable
    public data class AttributionReportingAggregatableDebugReportingData(
        public val keyPiece: String,
        /**
         * number instead of integer because not all uint32 can be represented by
         * int
         */
        public val `value`: Double,
        public val types: List<String>,
    )

    @Serializable
    public data class AttributionReportingAggregatableDebugReportingConfig(
        /**
         * number instead of integer because not all uint32 can be represented by
         * int, only present for source registrations
         */
        public val budget: Double? = null,
        public val keyPiece: String,
        public val debugData: List<AttributionReportingAggregatableDebugReportingData>,
        public val aggregationCoordinatorOrigin: String? = null,
    )

    @Serializable
    public data class AttributionScopesData(
        public val values: List<String>,
        /**
         * number instead of integer because not all uint32 can be represented by
         * int
         */
        public val limit: Double,
        public val maxEventStates: Double,
    )

    @Serializable
    public data class AttributionReportingNamedBudgetDef(
        public val name: String,
        public val budget: Int,
    )

    @Serializable
    public data class AttributionReportingSourceRegistration(
        public val time: Double,
        /**
         * duration in seconds
         */
        public val expiry: Int,
        /**
         * number instead of integer because not all uint32 can be represented by
         * int
         */
        public val triggerData: List<Double>,
        public val eventReportWindows: AttributionReportingEventReportWindows,
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
        public val destinationLimitPriority: String,
        public val aggregatableDebugReportingConfig:
        AttributionReportingAggregatableDebugReportingConfig,
        public val scopesData: AttributionScopesData? = null,
        public val maxEventLevelReports: Int,
        public val namedBudgets: List<AttributionReportingNamedBudgetDef>,
        public val debugReporting: Boolean,
        public val eventLevelEpsilon: Double,
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

        @SerialName("exceedsMaxScopesChannelCapacity")
        EXCEEDSMAXSCOPESCHANNELCAPACITY,

        @SerialName("exceedsMaxTriggerStateCardinality")
        EXCEEDSMAXTRIGGERSTATECARDINALITY,

        @SerialName("exceedsMaxEventStatesLimit")
        EXCEEDSMAXEVENTSTATESLIMIT,

        @SerialName("destinationPerDayReportingLimitReached")
        DESTINATIONPERDAYREPORTINGLIMITREACHED,
    }

    @Serializable
    public enum class AttributionReportingSourceRegistrationTimeConfig {
        @SerialName("include")
        INCLUDE,

        @SerialName("exclude")
        EXCLUDE,
    }

    @Serializable
    public data class AttributionReportingAggregatableValueDictEntry(
        public val key: String,
        /**
         * number instead of integer because not all uint32 can be represented by
         * int
         */
        public val `value`: Double,
        public val filteringId: String,
    )

    @Serializable
    public data class AttributionReportingAggregatableValueEntry(
        public val values: List<AttributionReportingAggregatableValueDictEntry>,
        public val filters: AttributionReportingFilterPair,
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
    public data class AttributionReportingNamedBudgetCandidate(
        public val name: String? = null,
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
        public val aggregatableFilteringIdMaxBytes: Int,
        public val debugReporting: Boolean,
        public val aggregationCoordinatorOrigin: String? = null,
        public val sourceRegistrationTimeConfig: AttributionReportingSourceRegistrationTimeConfig,
        public val triggerContextId: String? = null,
        public val aggregatableDebugReportingConfig:
        AttributionReportingAggregatableDebugReportingConfig,
        public val scopes: List<String>,
        public val namedBudgets: List<AttributionReportingNamedBudgetCandidate>,
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

        @SerialName("insufficientNamedBudget")
        INSUFFICIENTNAMEDBUDGET,

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

    @Serializable
    public enum class AttributionReportingReportResult {
        @SerialName("sent")
        SENT,

        @SerialName("prohibited")
        PROHIBITED,

        @SerialName("failedToAssemble")
        FAILEDTOASSEMBLE,

        @SerialName("expired")
        EXPIRED,
    }

    /**
     * A single Related Website Set object.
     */
    @Serializable
    public data class RelatedWebsiteSet(
        /**
         * The primary site of this set, along with the ccTLDs if there is any.
         */
        public val primarySites: List<String>,
        /**
         * The associated sites of this set, along with the ccTLDs if there is any.
         */
        public val associatedSites: List<String>,
        /**
         * The service sites of this set, along with the ccTLDs if there is any.
         */
        public val serviceSites: List<String>,
    )

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
     * One of the interest groups was accessed. Note that these events are global
     * to all targets sharing an interest group store.
     */
    @Serializable
    public data class InterestGroupAccessedParameter(
        public val accessTime: Double,
        public val type: InterestGroupAccessType,
        public val ownerOrigin: String,
        public val name: String,
        /**
         * For topLevelBid/topLevelAdditionalBid, and when appropriate,
         * win and additionalBidWin
         */
        public val componentSellerOrigin: String? = null,
        /**
         * For bid or somethingBid event, if done locally and not on a server.
         */
        public val bid: Double? = null,
        public val bidCurrency: String? = null,
        /**
         * For non-global events --- links to interestGroupAuctionEvent
         */
        public val uniqueAuctionId: String? = null,
    )

    /**
     * An auction involving interest groups is taking place. These events are
     * target-specific.
     */
    @Serializable
    public data class InterestGroupAuctionEventOccurredParameter(
        public val eventTime: Double,
        public val type: InterestGroupAuctionEventType,
        public val uniqueAuctionId: String,
        /**
         * Set for child auctions.
         */
        public val parentAuctionId: String? = null,
        /**
         * Set for started and configResolved
         */
        public val auctionConfig: Map<String, JsonElement>? = null,
    )

    /**
     * Specifies which auctions a particular network fetch may be related to, and
     * in what role. Note that it is not ordered with respect to
     * Network.requestWillBeSent (but will happen before loadingFinished
     * loadingFailed).
     */
    @Serializable
    public data class InterestGroupAuctionNetworkRequestCreatedParameter(
        public val type: InterestGroupAuctionFetchType,
        public val requestId: String,
        /**
         * This is the set of the auctions using the worklet that issued this
         * request.  In the case of trusted signals, it's possible that only some of
         * them actually care about the keys being queried.
         */
        public val auctions: List<String>,
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
         * Enum value indicating the access scope.
         */
        public val scope: SharedStorageAccessScope,
        /**
         * Enum value indicating the Shared Storage API method invoked.
         */
        public val method: SharedStorageAccessMethod,
        /**
         * DevTools Frame Token for the primary frame tree's root.
         */
        public val mainFrameId: String,
        /**
         * Serialization of the origin owning the Shared Storage data.
         */
        public val ownerOrigin: String,
        /**
         * Serialization of the site owning the Shared Storage data.
         */
        public val ownerSite: String,
        /**
         * The sub-parameters wrapped by `params` are all optional and their
         * presence/absence depends on `type`.
         */
        public val params: SharedStorageAccessParams,
    )

    /**
     * A shared storage run or selectURL operation finished its execution.
     * The following parameters are included in all events.
     */
    @Serializable
    public data class SharedStorageWorkletOperationExecutionFinishedParameter(
        /**
         * Time that the operation finished.
         */
        public val finishedTime: Double,
        /**
         * Time, in microseconds, from start of shared storage JS API call until
         * end of operation execution in the worklet.
         */
        public val executionTime: Int,
        /**
         * Enum value indicating the Shared Storage API method invoked.
         */
        public val method: SharedStorageAccessMethod,
        /**
         * ID of the operation call.
         */
        public val operationId: String,
        /**
         * Hex representation of the DevTools token used as the TargetID for the
         * associated shared storage worklet.
         */
        public val workletTargetId: String,
        /**
         * DevTools Frame Token for the primary frame tree's root.
         */
        public val mainFrameId: String,
        /**
         * Serialization of the origin owning the Shared Storage data.
         */
        public val ownerOrigin: String,
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
    public data class AttributionReportingReportSentParameter(
        public val url: String,
        public val body: Map<String, JsonElement>,
        public val result: AttributionReportingReportResult,
        /**
         * If result is `sent`, populated with net/HTTP status.
         */
        public val netError: Int? = null,
        public val netErrorName: String? = null,
        public val httpStatusCode: Int? = null,
    )

    @Serializable
    public data class AttributionReportingVerboseDebugReportSentParameter(
        public val url: String,
        public val body: List<Map<String, JsonElement>>? = null,
        public val netError: Int? = null,
        public val netErrorName: String? = null,
        public val httpStatusCode: Int? = null,
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
        /**
         * This largely corresponds to:
         * https://wicg.github.io/turtledove/#dictdef-generatebidinterestgroup
         * but has absolute expirationTime instead of relative lifetimeMs and
         * also adds joiningOrigin.
         */
        public val details: Map<String, JsonElement>,
    )

    @Serializable
    public data class SetInterestGroupTrackingParameter(
        public val enable: Boolean,
    )

    @Serializable
    public data class SetInterestGroupAuctionTrackingParameter(
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

    @Serializable
    public data class SendPendingAttributionReportsReturn(
        /**
         * The number of reports that were sent.
         */
        public val numSent: Int,
    )

    @Serializable
    public data class GetRelatedWebsiteSetsReturn(
        public val sets: List<RelatedWebsiteSet>,
    )

    @Serializable
    public data class GetAffectedUrlsForThirdPartyCookieMetadataParameter(
        /**
         * The URL of the page currently being visited.
         */
        public val firstPartyUrl: String,
        /**
         * The list of embedded resource URLs from the page.
         */
        public val thirdPartyUrls: List<String>,
    )

    @Serializable
    public data class GetAffectedUrlsForThirdPartyCookieMetadataReturn(
        /**
         * Array of matching URLs. If there is a primary pattern match for the first-
         * party URL, only the first-party URL is returned in the array.
         */
        public val matchedUrls: List<String>,
    )

    @Serializable
    public data class SetProtectedAudienceKAnonymityParameter(
        public val owner: String,
        public val name: String,
        public val hashes: List<String>,
    )
}
