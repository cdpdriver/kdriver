@file:Suppress("ALL")

package dev.kdriver.cdp.domain

import dev.kaccelero.serializers.Serialization
import dev.kdriver.cdp.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.decodeFromJsonElement

public val CDP.preload: Preload
    get() = getGeneratedDomain() ?: cacheGeneratedDomain(Preload(this))

public class Preload(
    private val cdp: CDP,
) : Domain {
    /**
     * Upsert. Currently, it is only emitted when a rule set added.
     */
    public val ruleSetUpdated: Flow<RuleSetUpdatedParameter> = cdp
        .events
        .filter { it.method == "Preload.ruleSetUpdated" }
        .map { it.params }
        .filterNotNull()
        .map { Serialization.json.decodeFromJsonElement(it) }

    public val ruleSetRemoved: Flow<RuleSetRemovedParameter> = cdp
        .events
        .filter { it.method == "Preload.ruleSetRemoved" }
        .map { it.params }
        .filterNotNull()
        .map { Serialization.json.decodeFromJsonElement(it) }

    /**
     * Fired when a preload enabled state is updated.
     */
    public val preloadEnabledStateUpdated: Flow<PreloadEnabledStateUpdatedParameter> = cdp
        .events
        .filter { it.method == "Preload.preloadEnabledStateUpdated" }
        .map { it.params }
        .filterNotNull()
        .map { Serialization.json.decodeFromJsonElement(it) }

    /**
     * Fired when a prefetch attempt is updated.
     */
    public val prefetchStatusUpdated: Flow<PrefetchStatusUpdatedParameter> = cdp
        .events
        .filter { it.method == "Preload.prefetchStatusUpdated" }
        .map { it.params }
        .filterNotNull()
        .map { Serialization.json.decodeFromJsonElement(it) }

    /**
     * Fired when a prerender attempt is updated.
     */
    public val prerenderStatusUpdated: Flow<PrerenderStatusUpdatedParameter> = cdp
        .events
        .filter { it.method == "Preload.prerenderStatusUpdated" }
        .map { it.params }
        .filterNotNull()
        .map { Serialization.json.decodeFromJsonElement(it) }

    /**
     * Send a list of sources for all preloading attempts in a document.
     */
    public val preloadingAttemptSourcesUpdated: Flow<PreloadingAttemptSourcesUpdatedParameter> = cdp
        .events
        .filter { it.method == "Preload.preloadingAttemptSourcesUpdated" }
        .map { it.params }
        .filterNotNull()
        .map { Serialization.json.decodeFromJsonElement(it) }

    public suspend fun enable(mode: CommandMode = CommandMode.DEFAULT) {
        val parameter = null
        cdp.callCommand("Preload.enable", parameter, mode)
    }

    public suspend fun disable(mode: CommandMode = CommandMode.DEFAULT) {
        val parameter = null
        cdp.callCommand("Preload.disable", parameter, mode)
    }

    /**
     * Corresponds to SpeculationRuleSet
     */
    @Serializable
    public data class RuleSet(
        public val id: String,
        /**
         * Identifies a document which the rule set is associated with.
         */
        public val loaderId: String,
        /**
         * Source text of JSON representing the rule set. If it comes from
         * `<script>` tag, it is the textContent of the node. Note that it is
         * a JSON for valid case.
         *
         * See also:
         * - https://wicg.github.io/nav-speculation/speculation-rules.html
         * - https://github.com/WICG/nav-speculation/blob/main/triggers.md
         */
        public val sourceText: String,
        /**
         * A speculation rule set is either added through an inline
         * `<script>` tag or through an external resource via the
         * 'Speculation-Rules' HTTP header. For the first case, we include
         * the BackendNodeId of the relevant `<script>` tag. For the second
         * case, we include the external URL where the rule set was loaded
         * from, and also RequestId if Network domain is enabled.
         *
         * See also:
         * - https://wicg.github.io/nav-speculation/speculation-rules.html#speculation-rules-script
         * - https://wicg.github.io/nav-speculation/speculation-rules.html#speculation-rules-header
         */
        public val backendNodeId: Int? = null,
        public val url: String? = null,
        public val requestId: String? = null,
        /**
         * Error information
         * `errorMessage` is null iff `errorType` is null.
         */
        public val errorType: RuleSetErrorType? = null,
        /**
         * TODO(https://crbug.com/1425354): Replace this property with structured error.
         */
        public val errorMessage: String? = null,
    )

    @Serializable
    public enum class RuleSetErrorType {
        @SerialName("SourceIsNotJsonObject")
        SOURCEISNOTJSONOBJECT,

        @SerialName("InvalidRulesSkipped")
        INVALIDRULESSKIPPED,

        @SerialName("InvalidRulesetLevelTag")
        INVALIDRULESETLEVELTAG,
    }

    /**
     * The type of preloading attempted. It corresponds to
     * mojom::SpeculationAction (although PrefetchWithSubresources is omitted as it
     * isn't being used by clients).
     */
    @Serializable
    public enum class SpeculationAction {
        @SerialName("Prefetch")
        PREFETCH,

        @SerialName("Prerender")
        PRERENDER,
    }

    /**
     * Corresponds to mojom::SpeculationTargetHint.
     * See https://github.com/WICG/nav-speculation/blob/main/triggers.md#window-name-targeting-hints
     */
    @Serializable
    public enum class SpeculationTargetHint {
        @SerialName("Blank")
        BLANK,

        @SerialName("Self")
        SELF,
    }

    /**
     * A key that identifies a preloading attempt.
     *
     * The url used is the url specified by the trigger (i.e. the initial URL), and
     * not the final url that is navigated to. For example, prerendering allows
     * same-origin main frame navigations during the attempt, but the attempt is
     * still keyed with the initial URL.
     */
    @Serializable
    public data class PreloadingAttemptKey(
        public val loaderId: String,
        public val action: SpeculationAction,
        public val url: String,
        public val targetHint: SpeculationTargetHint? = null,
    )

    /**
     * Lists sources for a preloading attempt, specifically the ids of rule sets
     * that had a speculation rule that triggered the attempt, and the
     * BackendNodeIds of <a href> or <area href> elements that triggered the
     * attempt (in the case of attempts triggered by a document rule). It is
     * possible for multiple rule sets and links to trigger a single attempt.
     */
    @Serializable
    public data class PreloadingAttemptSource(
        public val key: PreloadingAttemptKey,
        public val ruleSetIds: List<String>,
        public val nodeIds: List<Int>,
    )

    /**
     * List of FinalStatus reasons for Prerender2.
     */
    @Serializable
    public enum class PrerenderFinalStatus {
        @SerialName("Activated")
        ACTIVATED,

        @SerialName("Destroyed")
        DESTROYED,

        @SerialName("LowEndDevice")
        LOWENDDEVICE,

        @SerialName("InvalidSchemeRedirect")
        INVALIDSCHEMEREDIRECT,

        @SerialName("InvalidSchemeNavigation")
        INVALIDSCHEMENAVIGATION,

        @SerialName("NavigationRequestBlockedByCsp")
        NAVIGATIONREQUESTBLOCKEDBYCSP,

        @SerialName("MojoBinderPolicy")
        MOJOBINDERPOLICY,

        @SerialName("RendererProcessCrashed")
        RENDERERPROCESSCRASHED,

        @SerialName("RendererProcessKilled")
        RENDERERPROCESSKILLED,

        @SerialName("Download")
        DOWNLOAD,

        @SerialName("TriggerDestroyed")
        TRIGGERDESTROYED,

        @SerialName("NavigationNotCommitted")
        NAVIGATIONNOTCOMMITTED,

        @SerialName("NavigationBadHttpStatus")
        NAVIGATIONBADHTTPSTATUS,

        @SerialName("ClientCertRequested")
        CLIENTCERTREQUESTED,

        @SerialName("NavigationRequestNetworkError")
        NAVIGATIONREQUESTNETWORKERROR,

        @SerialName("CancelAllHostsForTesting")
        CANCELALLHOSTSFORTESTING,

        @SerialName("DidFailLoad")
        DIDFAILLOAD,

        @SerialName("Stop")
        STOP,

        @SerialName("SslCertificateError")
        SSLCERTIFICATEERROR,

        @SerialName("LoginAuthRequested")
        LOGINAUTHREQUESTED,

        @SerialName("UaChangeRequiresReload")
        UACHANGEREQUIRESRELOAD,

        @SerialName("BlockedByClient")
        BLOCKEDBYCLIENT,

        @SerialName("AudioOutputDeviceRequested")
        AUDIOOUTPUTDEVICEREQUESTED,

        @SerialName("MixedContent")
        MIXEDCONTENT,

        @SerialName("TriggerBackgrounded")
        TRIGGERBACKGROUNDED,

        @SerialName("MemoryLimitExceeded")
        MEMORYLIMITEXCEEDED,

        @SerialName("DataSaverEnabled")
        DATASAVERENABLED,

        @SerialName("TriggerUrlHasEffectiveUrl")
        TRIGGERURLHASEFFECTIVEURL,

        @SerialName("ActivatedBeforeStarted")
        ACTIVATEDBEFORESTARTED,

        @SerialName("InactivePageRestriction")
        INACTIVEPAGERESTRICTION,

        @SerialName("StartFailed")
        STARTFAILED,

        @SerialName("TimeoutBackgrounded")
        TIMEOUTBACKGROUNDED,

        @SerialName("CrossSiteRedirectInInitialNavigation")
        CROSSSITEREDIRECTININITIALNAVIGATION,

        @SerialName("CrossSiteNavigationInInitialNavigation")
        CROSSSITENAVIGATIONININITIALNAVIGATION,

        @SerialName("SameSiteCrossOriginRedirectNotOptInInInitialNavigation")
        SAMESITECROSSORIGINREDIRECTNOTOPTINININITIALNAVIGATION,

        @SerialName("SameSiteCrossOriginNavigationNotOptInInInitialNavigation")
        SAMESITECROSSORIGINNAVIGATIONNOTOPTINININITIALNAVIGATION,

        @SerialName("ActivationNavigationParameterMismatch")
        ACTIVATIONNAVIGATIONPARAMETERMISMATCH,

        @SerialName("ActivatedInBackground")
        ACTIVATEDINBACKGROUND,

        @SerialName("EmbedderHostDisallowed")
        EMBEDDERHOSTDISALLOWED,

        @SerialName("ActivationNavigationDestroyedBeforeSuccess")
        ACTIVATIONNAVIGATIONDESTROYEDBEFORESUCCESS,

        @SerialName("TabClosedByUserGesture")
        TABCLOSEDBYUSERGESTURE,

        @SerialName("TabClosedWithoutUserGesture")
        TABCLOSEDWITHOUTUSERGESTURE,

        @SerialName("PrimaryMainFrameRendererProcessCrashed")
        PRIMARYMAINFRAMERENDERERPROCESSCRASHED,

        @SerialName("PrimaryMainFrameRendererProcessKilled")
        PRIMARYMAINFRAMERENDERERPROCESSKILLED,

        @SerialName("ActivationFramePolicyNotCompatible")
        ACTIVATIONFRAMEPOLICYNOTCOMPATIBLE,

        @SerialName("PreloadingDisabled")
        PRELOADINGDISABLED,

        @SerialName("BatterySaverEnabled")
        BATTERYSAVERENABLED,

        @SerialName("ActivatedDuringMainFrameNavigation")
        ACTIVATEDDURINGMAINFRAMENAVIGATION,

        @SerialName("PreloadingUnsupportedByWebContents")
        PRELOADINGUNSUPPORTEDBYWEBCONTENTS,

        @SerialName("CrossSiteRedirectInMainFrameNavigation")
        CROSSSITEREDIRECTINMAINFRAMENAVIGATION,

        @SerialName("CrossSiteNavigationInMainFrameNavigation")
        CROSSSITENAVIGATIONINMAINFRAMENAVIGATION,

        @SerialName("SameSiteCrossOriginRedirectNotOptInInMainFrameNavigation")
        SAMESITECROSSORIGINREDIRECTNOTOPTININMAINFRAMENAVIGATION,

        @SerialName("SameSiteCrossOriginNavigationNotOptInInMainFrameNavigation")
        SAMESITECROSSORIGINNAVIGATIONNOTOPTININMAINFRAMENAVIGATION,

        @SerialName("MemoryPressureOnTrigger")
        MEMORYPRESSUREONTRIGGER,

        @SerialName("MemoryPressureAfterTriggered")
        MEMORYPRESSUREAFTERTRIGGERED,

        @SerialName("PrerenderingDisabledByDevTools")
        PRERENDERINGDISABLEDBYDEVTOOLS,

        @SerialName("SpeculationRuleRemoved")
        SPECULATIONRULEREMOVED,

        @SerialName("ActivatedWithAuxiliaryBrowsingContexts")
        ACTIVATEDWITHAUXILIARYBROWSINGCONTEXTS,

        @SerialName("MaxNumOfRunningEagerPrerendersExceeded")
        MAXNUMOFRUNNINGEAGERPRERENDERSEXCEEDED,

        @SerialName("MaxNumOfRunningNonEagerPrerendersExceeded")
        MAXNUMOFRUNNINGNONEAGERPRERENDERSEXCEEDED,

        @SerialName("MaxNumOfRunningEmbedderPrerendersExceeded")
        MAXNUMOFRUNNINGEMBEDDERPRERENDERSEXCEEDED,

        @SerialName("PrerenderingUrlHasEffectiveUrl")
        PRERENDERINGURLHASEFFECTIVEURL,

        @SerialName("RedirectedPrerenderingUrlHasEffectiveUrl")
        REDIRECTEDPRERENDERINGURLHASEFFECTIVEURL,

        @SerialName("ActivationUrlHasEffectiveUrl")
        ACTIVATIONURLHASEFFECTIVEURL,

        @SerialName("JavaScriptInterfaceAdded")
        JAVASCRIPTINTERFACEADDED,

        @SerialName("JavaScriptInterfaceRemoved")
        JAVASCRIPTINTERFACEREMOVED,

        @SerialName("AllPrerenderingCanceled")
        ALLPRERENDERINGCANCELED,

        @SerialName("WindowClosed")
        WINDOWCLOSED,

        @SerialName("SlowNetwork")
        SLOWNETWORK,

        @SerialName("OtherPrerenderedPageActivated")
        OTHERPRERENDEREDPAGEACTIVATED,

        @SerialName("V8OptimizerDisabled")
        V8OPTIMIZERDISABLED,

        @SerialName("PrerenderFailedDuringPrefetch")
        PRERENDERFAILEDDURINGPREFETCH,

        @SerialName("BrowsingDataRemoved")
        BROWSINGDATAREMOVED,

        @SerialName("PrerenderHostReused")
        PRERENDERHOSTREUSED,
    }

    /**
     * Preloading status values, see also PreloadingTriggeringOutcome. This
     * status is shared by prefetchStatusUpdated and prerenderStatusUpdated.
     */
    @Serializable
    public enum class PreloadingStatus {
        @SerialName("Pending")
        PENDING,

        @SerialName("Running")
        RUNNING,

        @SerialName("Ready")
        READY,

        @SerialName("Success")
        SUCCESS,

        @SerialName("Failure")
        FAILURE,

        @SerialName("NotSupported")
        NOTSUPPORTED,
    }

    /**
     * TODO(https://crbug.com/1384419): revisit the list of PrefetchStatus and
     * filter out the ones that aren't necessary to the developers.
     */
    @Serializable
    public enum class PrefetchStatus {
        @SerialName("PrefetchAllowed")
        PREFETCHALLOWED,

        @SerialName("PrefetchFailedIneligibleRedirect")
        PREFETCHFAILEDINELIGIBLEREDIRECT,

        @SerialName("PrefetchFailedInvalidRedirect")
        PREFETCHFAILEDINVALIDREDIRECT,

        @SerialName("PrefetchFailedMIMENotSupported")
        PREFETCHFAILEDMIMENOTSUPPORTED,

        @SerialName("PrefetchFailedNetError")
        PREFETCHFAILEDNETERROR,

        @SerialName("PrefetchFailedNon2XX")
        PREFETCHFAILEDNON2XX,

        @SerialName("PrefetchEvictedAfterBrowsingDataRemoved")
        PREFETCHEVICTEDAFTERBROWSINGDATAREMOVED,

        @SerialName("PrefetchEvictedAfterCandidateRemoved")
        PREFETCHEVICTEDAFTERCANDIDATEREMOVED,

        @SerialName("PrefetchEvictedForNewerPrefetch")
        PREFETCHEVICTEDFORNEWERPREFETCH,

        @SerialName("PrefetchHeldback")
        PREFETCHHELDBACK,

        @SerialName("PrefetchIneligibleRetryAfter")
        PREFETCHINELIGIBLERETRYAFTER,

        @SerialName("PrefetchIsPrivacyDecoy")
        PREFETCHISPRIVACYDECOY,

        @SerialName("PrefetchIsStale")
        PREFETCHISSTALE,

        @SerialName("PrefetchNotEligibleBrowserContextOffTheRecord")
        PREFETCHNOTELIGIBLEBROWSERCONTEXTOFFTHERECORD,

        @SerialName("PrefetchNotEligibleDataSaverEnabled")
        PREFETCHNOTELIGIBLEDATASAVERENABLED,

        @SerialName("PrefetchNotEligibleExistingProxy")
        PREFETCHNOTELIGIBLEEXISTINGPROXY,

        @SerialName("PrefetchNotEligibleHostIsNonUnique")
        PREFETCHNOTELIGIBLEHOSTISNONUNIQUE,

        @SerialName("PrefetchNotEligibleNonDefaultStoragePartition")
        PREFETCHNOTELIGIBLENONDEFAULTSTORAGEPARTITION,

        @SerialName("PrefetchNotEligibleSameSiteCrossOriginPrefetchRequiredProxy")
        PREFETCHNOTELIGIBLESAMESITECROSSORIGINPREFETCHREQUIREDPROXY,

        @SerialName("PrefetchNotEligibleSchemeIsNotHttps")
        PREFETCHNOTELIGIBLESCHEMEISNOTHTTPS,

        @SerialName("PrefetchNotEligibleUserHasCookies")
        PREFETCHNOTELIGIBLEUSERHASCOOKIES,

        @SerialName("PrefetchNotEligibleUserHasServiceWorker")
        PREFETCHNOTELIGIBLEUSERHASSERVICEWORKER,

        @SerialName("PrefetchNotEligibleUserHasServiceWorkerNoFetchHandler")
        PREFETCHNOTELIGIBLEUSERHASSERVICEWORKERNOFETCHHANDLER,

        @SerialName("PrefetchNotEligibleRedirectFromServiceWorker")
        PREFETCHNOTELIGIBLEREDIRECTFROMSERVICEWORKER,

        @SerialName("PrefetchNotEligibleRedirectToServiceWorker")
        PREFETCHNOTELIGIBLEREDIRECTTOSERVICEWORKER,

        @SerialName("PrefetchNotEligibleBatterySaverEnabled")
        PREFETCHNOTELIGIBLEBATTERYSAVERENABLED,

        @SerialName("PrefetchNotEligiblePreloadingDisabled")
        PREFETCHNOTELIGIBLEPRELOADINGDISABLED,

        @SerialName("PrefetchNotFinishedInTime")
        PREFETCHNOTFINISHEDINTIME,

        @SerialName("PrefetchNotStarted")
        PREFETCHNOTSTARTED,

        @SerialName("PrefetchNotUsedCookiesChanged")
        PREFETCHNOTUSEDCOOKIESCHANGED,

        @SerialName("PrefetchProxyNotAvailable")
        PREFETCHPROXYNOTAVAILABLE,

        @SerialName("PrefetchResponseUsed")
        PREFETCHRESPONSEUSED,

        @SerialName("PrefetchSuccessfulButNotUsed")
        PREFETCHSUCCESSFULBUTNOTUSED,

        @SerialName("PrefetchNotUsedProbeFailed")
        PREFETCHNOTUSEDPROBEFAILED,
    }

    /**
     * Information of headers to be displayed when the header mismatch occurred.
     */
    @Serializable
    public data class PrerenderMismatchedHeaders(
        public val headerName: String,
        public val initialValue: String? = null,
        public val activationValue: String? = null,
    )

    /**
     * Upsert. Currently, it is only emitted when a rule set added.
     */
    @Serializable
    public data class RuleSetUpdatedParameter(
        public val ruleSet: RuleSet,
    )

    @Serializable
    public data class RuleSetRemovedParameter(
        public val id: String,
    )

    /**
     * Fired when a preload enabled state is updated.
     */
    @Serializable
    public data class PreloadEnabledStateUpdatedParameter(
        public val disabledByPreference: Boolean,
        public val disabledByDataSaver: Boolean,
        public val disabledByBatterySaver: Boolean,
        public val disabledByHoldbackPrefetchSpeculationRules: Boolean,
        public val disabledByHoldbackPrerenderSpeculationRules: Boolean,
    )

    /**
     * Fired when a prefetch attempt is updated.
     */
    @Serializable
    public data class PrefetchStatusUpdatedParameter(
        public val key: PreloadingAttemptKey,
        public val pipelineId: String,
        /**
         * The frame id of the frame initiating prefetch.
         */
        public val initiatingFrameId: String,
        public val prefetchUrl: String,
        public val status: PreloadingStatus,
        public val prefetchStatus: PrefetchStatus,
        public val requestId: String,
    )

    /**
     * Fired when a prerender attempt is updated.
     */
    @Serializable
    public data class PrerenderStatusUpdatedParameter(
        public val key: PreloadingAttemptKey,
        public val pipelineId: String,
        public val status: PreloadingStatus,
        public val prerenderStatus: PrerenderFinalStatus? = null,
        /**
         * This is used to give users more information about the name of Mojo interface
         * that is incompatible with prerender and has caused the cancellation of the attempt.
         */
        public val disallowedMojoInterface: String? = null,
        public val mismatchedHeaders: List<PrerenderMismatchedHeaders>? = null,
    )

    /**
     * Send a list of sources for all preloading attempts in a document.
     */
    @Serializable
    public data class PreloadingAttemptSourcesUpdatedParameter(
        public val loaderId: String,
        public val preloadingAttemptSources: List<PreloadingAttemptSource>,
    )
}
