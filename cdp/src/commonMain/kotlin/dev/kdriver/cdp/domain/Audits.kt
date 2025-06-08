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

public val CDP.audits: Audits
    get() = getGeneratedDomain() ?: cacheGeneratedDomain(Audits(this))

/**
 * Audits domain allows investigation of page violations and possible improvements.
 */
public class Audits(
    private val cdp: CDP,
) : Domain {
    public val issueAdded: Flow<IssueAddedParameter> = cdp
        .events
        .filter { it.method == "Audits.issueAdded" }
        .map { it.params }
        .filterNotNull()
        .map { Serialization.json.decodeFromJsonElement(it) }

    /**
     * Returns the response body and size if it were re-encoded with the specified settings. Only
     * applies to images.
     */
    public suspend fun getEncodedResponse(args: GetEncodedResponseParameter): GetEncodedResponseReturn {
        val parameter = Serialization.json.encodeToJsonElement(args)
        val result = cdp.callCommand("Audits.getEncodedResponse", parameter)
        return result!!.let { Serialization.json.decodeFromJsonElement(it) }
    }

    /**
     * Returns the response body and size if it were re-encoded with the specified settings. Only
     * applies to images.
     *
     * @param requestId Identifier of the network request to get content for.
     * @param encoding The encoding to use.
     * @param quality The quality of the encoding (0-1). (defaults to 1)
     * @param sizeOnly Whether to only return the size information (defaults to false).
     */
    public suspend fun getEncodedResponse(
        requestId: String,
        encoding: String,
        quality: Double? = null,
        sizeOnly: Boolean? = null,
    ): GetEncodedResponseReturn {
        val parameter = GetEncodedResponseParameter(
            requestId = requestId,
            encoding = encoding,
            quality = quality,
            sizeOnly = sizeOnly
        )
        return getEncodedResponse(parameter)
    }

    /**
     * Disables issues domain, prevents further issues from being reported to the client.
     */
    public suspend fun disable() {
        val parameter = null
        cdp.callCommand("Audits.disable", parameter)
    }

    /**
     * Enables issues domain, sends the issues collected so far to the client by means of the
     * `issueAdded` event.
     */
    public suspend fun enable() {
        val parameter = null
        cdp.callCommand("Audits.enable", parameter)
    }

    /**
     * Runs the contrast check for the target page. Found issues are reported
     * using Audits.issueAdded event.
     */
    public suspend fun checkContrast(args: CheckContrastParameter) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("Audits.checkContrast", parameter)
    }

    /**
     * Runs the contrast check for the target page. Found issues are reported
     * using Audits.issueAdded event.
     *
     * @param reportAAA Whether to report WCAG AAA level issues. Default is false.
     */
    public suspend fun checkContrast(reportAAA: Boolean? = null) {
        val parameter = CheckContrastParameter(reportAAA = reportAAA)
        checkContrast(parameter)
    }

    /**
     * Runs the form issues check for the target page. Found issues are reported
     * using Audits.issueAdded event.
     */
    public suspend fun checkFormsIssues(): CheckFormsIssuesReturn {
        val parameter = null
        val result = cdp.callCommand("Audits.checkFormsIssues", parameter)
        return result!!.let { Serialization.json.decodeFromJsonElement(it) }
    }

    /**
     * Information about a cookie that is affected by an inspector issue.
     */
    @Serializable
    public data class AffectedCookie(
        /**
         * The following three properties uniquely identify a cookie
         */
        public val name: String,
        public val path: String,
        public val domain: String,
    )

    /**
     * Information about a request that is affected by an inspector issue.
     */
    @Serializable
    public data class AffectedRequest(
        /**
         * The unique request id.
         */
        public val requestId: String,
        public val url: String? = null,
    )

    /**
     * Information about the frame affected by an inspector issue.
     */
    @Serializable
    public data class AffectedFrame(
        public val frameId: String,
    )

    @Serializable
    public enum class CookieExclusionReason {
        @SerialName("ExcludeSameSiteUnspecifiedTreatedAsLax")
        EXCLUDESAMESITEUNSPECIFIEDTREATEDASLAX,

        @SerialName("ExcludeSameSiteNoneInsecure")
        EXCLUDESAMESITENONEINSECURE,

        @SerialName("ExcludeSameSiteLax")
        EXCLUDESAMESITELAX,

        @SerialName("ExcludeSameSiteStrict")
        EXCLUDESAMESITESTRICT,

        @SerialName("ExcludeInvalidSameParty")
        EXCLUDEINVALIDSAMEPARTY,

        @SerialName("ExcludeSamePartyCrossPartyContext")
        EXCLUDESAMEPARTYCROSSPARTYCONTEXT,

        @SerialName("ExcludeDomainNonASCII")
        EXCLUDEDOMAINNONASCII,

        @SerialName("ExcludeThirdPartyCookieBlockedInFirstPartySet")
        EXCLUDETHIRDPARTYCOOKIEBLOCKEDINFIRSTPARTYSET,

        @SerialName("ExcludeThirdPartyPhaseout")
        EXCLUDETHIRDPARTYPHASEOUT,
    }

    @Serializable
    public enum class CookieWarningReason {
        @SerialName("WarnSameSiteUnspecifiedCrossSiteContext")
        WARNSAMESITEUNSPECIFIEDCROSSSITECONTEXT,

        @SerialName("WarnSameSiteNoneInsecure")
        WARNSAMESITENONEINSECURE,

        @SerialName("WarnSameSiteUnspecifiedLaxAllowUnsafe")
        WARNSAMESITEUNSPECIFIEDLAXALLOWUNSAFE,

        @SerialName("WarnSameSiteStrictLaxDowngradeStrict")
        WARNSAMESITESTRICTLAXDOWNGRADESTRICT,

        @SerialName("WarnSameSiteStrictCrossDowngradeStrict")
        WARNSAMESITESTRICTCROSSDOWNGRADESTRICT,

        @SerialName("WarnSameSiteStrictCrossDowngradeLax")
        WARNSAMESITESTRICTCROSSDOWNGRADELAX,

        @SerialName("WarnSameSiteLaxCrossDowngradeStrict")
        WARNSAMESITELAXCROSSDOWNGRADESTRICT,

        @SerialName("WarnSameSiteLaxCrossDowngradeLax")
        WARNSAMESITELAXCROSSDOWNGRADELAX,

        @SerialName("WarnAttributeValueExceedsMaxSize")
        WARNATTRIBUTEVALUEEXCEEDSMAXSIZE,

        @SerialName("WarnDomainNonASCII")
        WARNDOMAINNONASCII,

        @SerialName("WarnThirdPartyPhaseout")
        WARNTHIRDPARTYPHASEOUT,

        @SerialName("WarnCrossSiteRedirectDowngradeChangesInclusion")
        WARNCROSSSITEREDIRECTDOWNGRADECHANGESINCLUSION,
    }

    @Serializable
    public enum class CookieOperation {
        @SerialName("SetCookie")
        SETCOOKIE,

        @SerialName("ReadCookie")
        READCOOKIE,
    }

    /**
     * This information is currently necessary, as the front-end has a difficult
     * time finding a specific cookie. With this, we can convey specific error
     * information without the cookie.
     */
    @Serializable
    public data class CookieIssueDetails(
        /**
         * If AffectedCookie is not set then rawCookieLine contains the raw
         * Set-Cookie header string. This hints at a problem where the
         * cookie line is syntactically or semantically malformed in a way
         * that no valid cookie could be created.
         */
        public val cookie: AffectedCookie? = null,
        public val rawCookieLine: String? = null,
        public val cookieWarningReasons: List<CookieWarningReason>,
        public val cookieExclusionReasons: List<CookieExclusionReason>,
        /**
         * Optionally identifies the site-for-cookies and the cookie url, which
         * may be used by the front-end as additional context.
         */
        public val operation: CookieOperation,
        public val siteForCookies: String? = null,
        public val cookieUrl: String? = null,
        public val request: AffectedRequest? = null,
    )

    @Serializable
    public enum class MixedContentResolutionStatus {
        @SerialName("MixedContentBlocked")
        MIXEDCONTENTBLOCKED,

        @SerialName("MixedContentAutomaticallyUpgraded")
        MIXEDCONTENTAUTOMATICALLYUPGRADED,

        @SerialName("MixedContentWarning")
        MIXEDCONTENTWARNING,
    }

    @Serializable
    public enum class MixedContentResourceType {
        @SerialName("AttributionSrc")
        ATTRIBUTIONSRC,

        @SerialName("Audio")
        AUDIO,

        @SerialName("Beacon")
        BEACON,

        @SerialName("CSPReport")
        CSPREPORT,

        @SerialName("Download")
        DOWNLOAD,

        @SerialName("EventSource")
        EVENTSOURCE,

        @SerialName("Favicon")
        FAVICON,

        @SerialName("Font")
        FONT,

        @SerialName("Form")
        FORM,

        @SerialName("Frame")
        FRAME,

        @SerialName("Image")
        IMAGE,

        @SerialName("Import")
        IMPORT,

        @SerialName("Manifest")
        MANIFEST,

        @SerialName("Ping")
        PING,

        @SerialName("PluginData")
        PLUGINDATA,

        @SerialName("PluginResource")
        PLUGINRESOURCE,

        @SerialName("Prefetch")
        PREFETCH,

        @SerialName("Resource")
        RESOURCE,

        @SerialName("Script")
        SCRIPT,

        @SerialName("ServiceWorker")
        SERVICEWORKER,

        @SerialName("SharedWorker")
        SHAREDWORKER,

        @SerialName("SpeculationRules")
        SPECULATIONRULES,

        @SerialName("Stylesheet")
        STYLESHEET,

        @SerialName("Track")
        TRACK,

        @SerialName("Video")
        VIDEO,

        @SerialName("Worker")
        WORKER,

        @SerialName("XMLHttpRequest")
        XMLHTTPREQUEST,

        @SerialName("XSLT")
        XSLT,
    }

    @Serializable
    public data class MixedContentIssueDetails(
        /**
         * The type of resource causing the mixed content issue (css, js, iframe,
         * form,...). Marked as optional because it is mapped to from
         * blink::mojom::RequestContextType, which will be replaced
         * by network::mojom::RequestDestination
         */
        public val resourceType: MixedContentResourceType? = null,
        /**
         * The way the mixed content issue is being resolved.
         */
        public val resolutionStatus: MixedContentResolutionStatus,
        /**
         * The unsafe http url causing the mixed content issue.
         */
        public val insecureURL: String,
        /**
         * The url responsible for the call to an unsafe url.
         */
        public val mainResourceURL: String,
        /**
         * The mixed content request.
         * Does not always exist (e.g. for unsafe form submission urls).
         */
        public val request: AffectedRequest? = null,
        /**
         * Optional because not every mixed content issue is necessarily linked to a frame.
         */
        public val frame: AffectedFrame? = null,
    )

    /**
     * Enum indicating the reason a response has been blocked. These reasons are
     * refinements of the net error BLOCKED_BY_RESPONSE.
     */
    @Serializable
    public enum class BlockedByResponseReason {
        @SerialName("CoepFrameResourceNeedsCoepHeader")
        COEPFRAMERESOURCENEEDSCOEPHEADER,

        @SerialName("CoopSandboxedIFrameCannotNavigateToCoopPage")
        COOPSANDBOXEDIFRAMECANNOTNAVIGATETOCOOPPAGE,

        @SerialName("CorpNotSameOrigin")
        CORPNOTSAMEORIGIN,

        @SerialName("CorpNotSameOriginAfterDefaultedToSameOriginByCoep")
        CORPNOTSAMEORIGINAFTERDEFAULTEDTOSAMEORIGINBYCOEP,

        @SerialName("CorpNotSameSite")
        CORPNOTSAMESITE,
    }

    /**
     * Details for a request that has been blocked with the BLOCKED_BY_RESPONSE
     * code. Currently only used for COEP/COOP, but may be extended to include
     * some CSP errors in the future.
     */
    @Serializable
    public data class BlockedByResponseIssueDetails(
        public val request: AffectedRequest,
        public val parentFrame: AffectedFrame? = null,
        public val blockedFrame: AffectedFrame? = null,
        public val reason: BlockedByResponseReason,
    )

    @Serializable
    public enum class HeavyAdResolutionStatus {
        @SerialName("HeavyAdBlocked")
        HEAVYADBLOCKED,

        @SerialName("HeavyAdWarning")
        HEAVYADWARNING,
    }

    @Serializable
    public enum class HeavyAdReason {
        @SerialName("NetworkTotalLimit")
        NETWORKTOTALLIMIT,

        @SerialName("CpuTotalLimit")
        CPUTOTALLIMIT,

        @SerialName("CpuPeakLimit")
        CPUPEAKLIMIT,
    }

    @Serializable
    public data class HeavyAdIssueDetails(
        /**
         * The resolution status, either blocking the content or warning.
         */
        public val resolution: HeavyAdResolutionStatus,
        /**
         * The reason the ad was blocked, total network or cpu or peak cpu.
         */
        public val reason: HeavyAdReason,
        /**
         * The frame that was blocked.
         */
        public val frame: AffectedFrame,
    )

    @Serializable
    public enum class ContentSecurityPolicyViolationType {
        @SerialName("kInlineViolation")
        KINLINEVIOLATION,

        @SerialName("kEvalViolation")
        KEVALVIOLATION,

        @SerialName("kURLViolation")
        KURLVIOLATION,

        @SerialName("kTrustedTypesSinkViolation")
        KTRUSTEDTYPESSINKVIOLATION,

        @SerialName("kTrustedTypesPolicyViolation")
        KTRUSTEDTYPESPOLICYVIOLATION,

        @SerialName("kWasmEvalViolation")
        KWASMEVALVIOLATION,
    }

    @Serializable
    public data class SourceCodeLocation(
        public val scriptId: String? = null,
        public val url: String,
        public val lineNumber: Int,
        public val columnNumber: Int,
    )

    @Serializable
    public data class ContentSecurityPolicyIssueDetails(
        /**
         * The url not included in allowed sources.
         */
        public val blockedURL: String? = null,
        /**
         * Specific directive that is violated, causing the CSP issue.
         */
        public val violatedDirective: String,
        public val isReportOnly: Boolean,
        public val contentSecurityPolicyViolationType: ContentSecurityPolicyViolationType,
        public val frameAncestor: AffectedFrame? = null,
        public val sourceCodeLocation: SourceCodeLocation? = null,
        public val violatingNodeId: Int? = null,
    )

    @Serializable
    public enum class SharedArrayBufferIssueType {
        @SerialName("TransferIssue")
        TRANSFERISSUE,

        @SerialName("CreationIssue")
        CREATIONISSUE,
    }

    /**
     * Details for a issue arising from an SAB being instantiated in, or
     * transferred to a context that is not cross-origin isolated.
     */
    @Serializable
    public data class SharedArrayBufferIssueDetails(
        public val sourceCodeLocation: SourceCodeLocation,
        public val isWarning: Boolean,
        public val type: SharedArrayBufferIssueType,
    )

    @Serializable
    public data class LowTextContrastIssueDetails(
        public val violatingNodeId: Int,
        public val violatingNodeSelector: String,
        public val contrastRatio: Double,
        public val thresholdAA: Double,
        public val thresholdAAA: Double,
        public val fontSize: String,
        public val fontWeight: String,
    )

    /**
     * Details for a CORS related issue, e.g. a warning or error related to
     * CORS RFC1918 enforcement.
     */
    @Serializable
    public data class CorsIssueDetails(
        public val corsErrorStatus: Network.CorsErrorStatus,
        public val isWarning: Boolean,
        public val request: AffectedRequest,
        public val location: SourceCodeLocation? = null,
        public val initiatorOrigin: String? = null,
        public val resourceIPAddressSpace: Network.IPAddressSpace? = null,
        public val clientSecurityState: Network.ClientSecurityState? = null,
    )

    @Serializable
    public enum class AttributionReportingIssueType {
        @SerialName("PermissionPolicyDisabled")
        PERMISSIONPOLICYDISABLED,

        @SerialName("UntrustworthyReportingOrigin")
        UNTRUSTWORTHYREPORTINGORIGIN,

        @SerialName("InsecureContext")
        INSECURECONTEXT,

        @SerialName("InvalidHeader")
        INVALIDHEADER,

        @SerialName("InvalidRegisterTriggerHeader")
        INVALIDREGISTERTRIGGERHEADER,

        @SerialName("SourceAndTriggerHeaders")
        SOURCEANDTRIGGERHEADERS,

        @SerialName("SourceIgnored")
        SOURCEIGNORED,

        @SerialName("TriggerIgnored")
        TRIGGERIGNORED,

        @SerialName("OsSourceIgnored")
        OSSOURCEIGNORED,

        @SerialName("OsTriggerIgnored")
        OSTRIGGERIGNORED,

        @SerialName("InvalidRegisterOsSourceHeader")
        INVALIDREGISTEROSSOURCEHEADER,

        @SerialName("InvalidRegisterOsTriggerHeader")
        INVALIDREGISTEROSTRIGGERHEADER,

        @SerialName("WebAndOsHeaders")
        WEBANDOSHEADERS,

        @SerialName("NoWebOrOsSupport")
        NOWEBOROSSUPPORT,

        @SerialName("NavigationRegistrationWithoutTransientUserActivation")
        NAVIGATIONREGISTRATIONWITHOUTTRANSIENTUSERACTIVATION,
    }

    /**
     * Details for issues around "Attribution Reporting API" usage.
     * Explainer: https://github.com/WICG/attribution-reporting-api
     */
    @Serializable
    public data class AttributionReportingIssueDetails(
        public val violationType: AttributionReportingIssueType,
        public val request: AffectedRequest? = null,
        public val violatingNodeId: Int? = null,
        public val invalidParameter: String? = null,
    )

    /**
     * Details for issues about documents in Quirks Mode
     * or Limited Quirks Mode that affects page layouting.
     */
    @Serializable
    public data class QuirksModeIssueDetails(
        /**
         * If false, it means the document's mode is "quirks"
         * instead of "limited-quirks".
         */
        public val isLimitedQuirksMode: Boolean,
        public val documentNodeId: Int,
        public val url: String,
        public val frameId: String,
        public val loaderId: String,
    )

    @Serializable
    public data class NavigatorUserAgentIssueDetails(
        public val url: String,
        public val location: SourceCodeLocation? = null,
    )

    @Serializable
    public enum class GenericIssueErrorType {
        @SerialName("CrossOriginPortalPostMessageError")
        CROSSORIGINPORTALPOSTMESSAGEERROR,

        @SerialName("FormLabelForNameError")
        FORMLABELFORNAMEERROR,

        @SerialName("FormDuplicateIdForInputError")
        FORMDUPLICATEIDFORINPUTERROR,

        @SerialName("FormInputWithNoLabelError")
        FORMINPUTWITHNOLABELERROR,

        @SerialName("FormAutocompleteAttributeEmptyError")
        FORMAUTOCOMPLETEATTRIBUTEEMPTYERROR,

        @SerialName("FormEmptyIdAndNameAttributesForInputError")
        FORMEMPTYIDANDNAMEATTRIBUTESFORINPUTERROR,

        @SerialName("FormAriaLabelledByToNonExistingId")
        FORMARIALABELLEDBYTONONEXISTINGID,

        @SerialName("FormInputAssignedAutocompleteValueToIdOrNameAttributeError")
        FORMINPUTASSIGNEDAUTOCOMPLETEVALUETOIDORNAMEATTRIBUTEERROR,

        @SerialName("FormLabelHasNeitherForNorNestedInput")
        FORMLABELHASNEITHERFORNORNESTEDINPUT,

        @SerialName("FormLabelForMatchesNonExistingIdError")
        FORMLABELFORMATCHESNONEXISTINGIDERROR,

        @SerialName("FormInputHasWrongButWellIntendedAutocompleteValueError")
        FORMINPUTHASWRONGBUTWELLINTENDEDAUTOCOMPLETEVALUEERROR,

        @SerialName("ResponseWasBlockedByORB")
        RESPONSEWASBLOCKEDBYORB,
    }

    /**
     * Depending on the concrete errorType, different properties are set.
     */
    @Serializable
    public data class GenericIssueDetails(
        /**
         * Issues with the same errorType are aggregated in the frontend.
         */
        public val errorType: GenericIssueErrorType,
        public val frameId: String? = null,
        public val violatingNodeId: Int? = null,
        public val violatingNodeAttribute: String? = null,
        public val request: AffectedRequest? = null,
    )

    /**
     * This issue tracks information needed to print a deprecation message.
     * https://source.chromium.org/chromium/chromium/src/+/main:third_party/blink/renderer/core/frame/third_party/blink/renderer/core/frame/deprecation/README.md
     */
    @Serializable
    public data class DeprecationIssueDetails(
        public val affectedFrame: AffectedFrame? = null,
        public val sourceCodeLocation: SourceCodeLocation,
        /**
         * One of the deprecation names from third_party/blink/renderer/core/frame/deprecation/deprecation.json5
         */
        public val type: String,
    )

    /**
     * This issue warns about sites in the redirect chain of a finished navigation
     * that may be flagged as trackers and have their state cleared if they don't
     * receive a user interaction. Note that in this context 'site' means eTLD+1.
     * For example, if the URL `https://example.test:80/bounce` was in the
     * redirect chain, the site reported would be `example.test`.
     */
    @Serializable
    public data class BounceTrackingIssueDetails(
        public val trackingSites: List<String>,
    )

    /**
     * This issue warns about third-party sites that are accessing cookies on the
     * current page, and have been permitted due to having a global metadata grant.
     * Note that in this context 'site' means eTLD+1. For example, if the URL
     * `https://example.test:80/web_page` was accessing cookies, the site reported
     * would be `example.test`.
     */
    @Serializable
    public data class CookieDeprecationMetadataIssueDetails(
        public val allowedSites: List<String>,
    )

    @Serializable
    public enum class ClientHintIssueReason {
        @SerialName("MetaTagAllowListInvalidOrigin")
        METATAGALLOWLISTINVALIDORIGIN,

        @SerialName("MetaTagModifiedHTML")
        METATAGMODIFIEDHTML,
    }

    @Serializable
    public data class FederatedAuthRequestIssueDetails(
        public val federatedAuthRequestIssueReason: FederatedAuthRequestIssueReason,
    )

    /**
     * Represents the failure reason when a federated authentication reason fails.
     * Should be updated alongside RequestIdTokenStatus in
     * third_party/blink/public/mojom/devtools/inspector_issue.mojom to include
     * all cases except for success.
     */
    @Serializable
    public enum class FederatedAuthRequestIssueReason {
        @SerialName("ShouldEmbargo")
        SHOULDEMBARGO,

        @SerialName("TooManyRequests")
        TOOMANYREQUESTS,

        @SerialName("WellKnownHttpNotFound")
        WELLKNOWNHTTPNOTFOUND,

        @SerialName("WellKnownNoResponse")
        WELLKNOWNNORESPONSE,

        @SerialName("WellKnownInvalidResponse")
        WELLKNOWNINVALIDRESPONSE,

        @SerialName("WellKnownListEmpty")
        WELLKNOWNLISTEMPTY,

        @SerialName("WellKnownInvalidContentType")
        WELLKNOWNINVALIDCONTENTTYPE,

        @SerialName("ConfigNotInWellKnown")
        CONFIGNOTINWELLKNOWN,

        @SerialName("WellKnownTooBig")
        WELLKNOWNTOOBIG,

        @SerialName("ConfigHttpNotFound")
        CONFIGHTTPNOTFOUND,

        @SerialName("ConfigNoResponse")
        CONFIGNORESPONSE,

        @SerialName("ConfigInvalidResponse")
        CONFIGINVALIDRESPONSE,

        @SerialName("ConfigInvalidContentType")
        CONFIGINVALIDCONTENTTYPE,

        @SerialName("ClientMetadataHttpNotFound")
        CLIENTMETADATAHTTPNOTFOUND,

        @SerialName("ClientMetadataNoResponse")
        CLIENTMETADATANORESPONSE,

        @SerialName("ClientMetadataInvalidResponse")
        CLIENTMETADATAINVALIDRESPONSE,

        @SerialName("ClientMetadataInvalidContentType")
        CLIENTMETADATAINVALIDCONTENTTYPE,

        @SerialName("DisabledInSettings")
        DISABLEDINSETTINGS,

        @SerialName("ErrorFetchingSignin")
        ERRORFETCHINGSIGNIN,

        @SerialName("InvalidSigninResponse")
        INVALIDSIGNINRESPONSE,

        @SerialName("AccountsHttpNotFound")
        ACCOUNTSHTTPNOTFOUND,

        @SerialName("AccountsNoResponse")
        ACCOUNTSNORESPONSE,

        @SerialName("AccountsInvalidResponse")
        ACCOUNTSINVALIDRESPONSE,

        @SerialName("AccountsListEmpty")
        ACCOUNTSLISTEMPTY,

        @SerialName("AccountsInvalidContentType")
        ACCOUNTSINVALIDCONTENTTYPE,

        @SerialName("IdTokenHttpNotFound")
        IDTOKENHTTPNOTFOUND,

        @SerialName("IdTokenNoResponse")
        IDTOKENNORESPONSE,

        @SerialName("IdTokenInvalidResponse")
        IDTOKENINVALIDRESPONSE,

        @SerialName("IdTokenIdpErrorResponse")
        IDTOKENIDPERRORRESPONSE,

        @SerialName("IdTokenCrossSiteIdpErrorResponse")
        IDTOKENCROSSSITEIDPERRORRESPONSE,

        @SerialName("IdTokenInvalidRequest")
        IDTOKENINVALIDREQUEST,

        @SerialName("IdTokenInvalidContentType")
        IDTOKENINVALIDCONTENTTYPE,

        @SerialName("ErrorIdToken")
        ERRORIDTOKEN,

        @SerialName("Canceled")
        CANCELED,

        @SerialName("RpPageNotVisible")
        RPPAGENOTVISIBLE,

        @SerialName("SilentMediationFailure")
        SILENTMEDIATIONFAILURE,

        @SerialName("ThirdPartyCookiesBlocked")
        THIRDPARTYCOOKIESBLOCKED,

        @SerialName("NotSignedInWithIdp")
        NOTSIGNEDINWITHIDP,
    }

    @Serializable
    public data class FederatedAuthUserInfoRequestIssueDetails(
        public val federatedAuthUserInfoRequestIssueReason: FederatedAuthUserInfoRequestIssueReason,
    )

    /**
     * Represents the failure reason when a getUserInfo() call fails.
     * Should be updated alongside FederatedAuthUserInfoRequestResult in
     * third_party/blink/public/mojom/devtools/inspector_issue.mojom.
     */
    @Serializable
    public enum class FederatedAuthUserInfoRequestIssueReason {
        @SerialName("NotSameOrigin")
        NOTSAMEORIGIN,

        @SerialName("NotIframe")
        NOTIFRAME,

        @SerialName("NotPotentiallyTrustworthy")
        NOTPOTENTIALLYTRUSTWORTHY,

        @SerialName("NoApiPermission")
        NOAPIPERMISSION,

        @SerialName("NotSignedInWithIdp")
        NOTSIGNEDINWITHIDP,

        @SerialName("NoAccountSharingPermission")
        NOACCOUNTSHARINGPERMISSION,

        @SerialName("InvalidConfigOrWellKnown")
        INVALIDCONFIGORWELLKNOWN,

        @SerialName("InvalidAccountsResponse")
        INVALIDACCOUNTSRESPONSE,

        @SerialName("NoReturningUserFromFetchedAccounts")
        NORETURNINGUSERFROMFETCHEDACCOUNTS,
    }

    /**
     * This issue tracks client hints related issues. It's used to deprecate old
     * features, encourage the use of new ones, and provide general guidance.
     */
    @Serializable
    public data class ClientHintIssueDetails(
        public val sourceCodeLocation: SourceCodeLocation,
        public val clientHintIssueReason: ClientHintIssueReason,
    )

    @Serializable
    public data class FailedRequestInfo(
        /**
         * The URL that failed to load.
         */
        public val url: String,
        /**
         * The failure message for the failed request.
         */
        public val failureMessage: String,
        public val requestId: String? = null,
    )

    @Serializable
    public enum class StyleSheetLoadingIssueReason {
        @SerialName("LateImportRule")
        LATEIMPORTRULE,

        @SerialName("RequestFailed")
        REQUESTFAILED,
    }

    /**
     * This issue warns when a referenced stylesheet couldn't be loaded.
     */
    @Serializable
    public data class StylesheetLoadingIssueDetails(
        /**
         * Source code position that referenced the failing stylesheet.
         */
        public val sourceCodeLocation: SourceCodeLocation,
        /**
         * Reason why the stylesheet couldn't be loaded.
         */
        public val styleSheetLoadingIssueReason: StyleSheetLoadingIssueReason,
        /**
         * Contains additional info when the failure was due to a request.
         */
        public val failedRequestInfo: FailedRequestInfo? = null,
    )

    @Serializable
    public enum class PropertyRuleIssueReason {
        @SerialName("InvalidSyntax")
        INVALIDSYNTAX,

        @SerialName("InvalidInitialValue")
        INVALIDINITIALVALUE,

        @SerialName("InvalidInherits")
        INVALIDINHERITS,

        @SerialName("InvalidName")
        INVALIDNAME,
    }

    /**
     * This issue warns about errors in property rules that lead to property
     * registrations being ignored.
     */
    @Serializable
    public data class PropertyRuleIssueDetails(
        /**
         * Source code position of the property rule.
         */
        public val sourceCodeLocation: SourceCodeLocation,
        /**
         * Reason why the property rule was discarded.
         */
        public val propertyRuleIssueReason: PropertyRuleIssueReason,
        /**
         * The value of the property rule property that failed to parse
         */
        public val propertyValue: String? = null,
    )

    /**
     * A unique identifier for the type of issue. Each type may use one of the
     * optional fields in InspectorIssueDetails to convey more specific
     * information about the kind of issue.
     */
    @Serializable
    public enum class InspectorIssueCode {
        @SerialName("CookieIssue")
        COOKIEISSUE,

        @SerialName("MixedContentIssue")
        MIXEDCONTENTISSUE,

        @SerialName("BlockedByResponseIssue")
        BLOCKEDBYRESPONSEISSUE,

        @SerialName("HeavyAdIssue")
        HEAVYADISSUE,

        @SerialName("ContentSecurityPolicyIssue")
        CONTENTSECURITYPOLICYISSUE,

        @SerialName("SharedArrayBufferIssue")
        SHAREDARRAYBUFFERISSUE,

        @SerialName("LowTextContrastIssue")
        LOWTEXTCONTRASTISSUE,

        @SerialName("CorsIssue")
        CORSISSUE,

        @SerialName("AttributionReportingIssue")
        ATTRIBUTIONREPORTINGISSUE,

        @SerialName("QuirksModeIssue")
        QUIRKSMODEISSUE,

        @SerialName("NavigatorUserAgentIssue")
        NAVIGATORUSERAGENTISSUE,

        @SerialName("GenericIssue")
        GENERICISSUE,

        @SerialName("DeprecationIssue")
        DEPRECATIONISSUE,

        @SerialName("ClientHintIssue")
        CLIENTHINTISSUE,

        @SerialName("FederatedAuthRequestIssue")
        FEDERATEDAUTHREQUESTISSUE,

        @SerialName("BounceTrackingIssue")
        BOUNCETRACKINGISSUE,

        @SerialName("CookieDeprecationMetadataIssue")
        COOKIEDEPRECATIONMETADATAISSUE,

        @SerialName("StylesheetLoadingIssue")
        STYLESHEETLOADINGISSUE,

        @SerialName("FederatedAuthUserInfoRequestIssue")
        FEDERATEDAUTHUSERINFOREQUESTISSUE,

        @SerialName("PropertyRuleIssue")
        PROPERTYRULEISSUE,
    }

    /**
     * This struct holds a list of optional fields with additional information
     * specific to the kind of issue. When adding a new issue code, please also
     * add a new optional field to this type.
     */
    @Serializable
    public data class InspectorIssueDetails(
        public val cookieIssueDetails: CookieIssueDetails? = null,
        public val mixedContentIssueDetails: MixedContentIssueDetails? = null,
        public val blockedByResponseIssueDetails: BlockedByResponseIssueDetails? = null,
        public val heavyAdIssueDetails: HeavyAdIssueDetails? = null,
        public val contentSecurityPolicyIssueDetails: ContentSecurityPolicyIssueDetails? = null,
        public val sharedArrayBufferIssueDetails: SharedArrayBufferIssueDetails? = null,
        public val lowTextContrastIssueDetails: LowTextContrastIssueDetails? = null,
        public val corsIssueDetails: CorsIssueDetails? = null,
        public val attributionReportingIssueDetails: AttributionReportingIssueDetails? = null,
        public val quirksModeIssueDetails: QuirksModeIssueDetails? = null,
        public val navigatorUserAgentIssueDetails: NavigatorUserAgentIssueDetails? = null,
        public val genericIssueDetails: GenericIssueDetails? = null,
        public val deprecationIssueDetails: DeprecationIssueDetails? = null,
        public val clientHintIssueDetails: ClientHintIssueDetails? = null,
        public val federatedAuthRequestIssueDetails: FederatedAuthRequestIssueDetails? = null,
        public val bounceTrackingIssueDetails: BounceTrackingIssueDetails? = null,
        public val cookieDeprecationMetadataIssueDetails:
        CookieDeprecationMetadataIssueDetails? = null,
        public val stylesheetLoadingIssueDetails: StylesheetLoadingIssueDetails? = null,
        public val propertyRuleIssueDetails: PropertyRuleIssueDetails? = null,
        public val federatedAuthUserInfoRequestIssueDetails:
        FederatedAuthUserInfoRequestIssueDetails? = null,
    )

    /**
     * An inspector issue reported from the back-end.
     */
    @Serializable
    public data class InspectorIssue(
        public val code: InspectorIssueCode,
        public val details: InspectorIssueDetails,
        /**
         * A unique id for this issue. May be omitted if no other entity (e.g.
         * exception, CDP message, etc.) is referencing this issue.
         */
        public val issueId: String? = null,
    )

    @Serializable
    public data class IssueAddedParameter(
        public val issue: InspectorIssue,
    )

    @Serializable
    public data class GetEncodedResponseParameter(
        /**
         * Identifier of the network request to get content for.
         */
        public val requestId: String,
        /**
         * The encoding to use.
         */
        public val encoding: String,
        /**
         * The quality of the encoding (0-1). (defaults to 1)
         */
        public val quality: Double? = null,
        /**
         * Whether to only return the size information (defaults to false).
         */
        public val sizeOnly: Boolean? = null,
    )

    @Serializable
    public data class GetEncodedResponseReturn(
        /**
         * The encoded body as a base64 string. Omitted if sizeOnly is true. (Encoded as a base64 string when passed over JSON)
         */
        public val body: String?,
        /**
         * Size before re-encoding.
         */
        public val originalSize: Int,
        /**
         * Size after re-encoding.
         */
        public val encodedSize: Int,
    )

    @Serializable
    public data class CheckContrastParameter(
        /**
         * Whether to report WCAG AAA level issues. Default is false.
         */
        public val reportAAA: Boolean? = null,
    )

    @Serializable
    public data class CheckFormsIssuesReturn(
        public val formIssues: List<GenericIssueDetails>,
    )
}
