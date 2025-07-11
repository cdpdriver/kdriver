package dev.kdriver.cdp.domain

import dev.kaccelero.serializers.Serialization
import dev.kdriver.cdp.CDP
import dev.kdriver.cdp.Domain
import dev.kdriver.cdp.cacheGeneratedDomain
import dev.kdriver.cdp.getGeneratedDomain
import kotlin.Boolean
import kotlin.Double
import kotlin.Int
import kotlin.String
import kotlin.collections.List
import kotlin.collections.Map
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.encodeToJsonElement

public val CDP.systemInfo: SystemInfo
    get() = getGeneratedDomain() ?: cacheGeneratedDomain(SystemInfo(this))

/**
 * The SystemInfo domain defines methods and events for querying low-level system information.
 */
public class SystemInfo(
    private val cdp: CDP,
) : Domain {
    /**
     * Returns information about the system.
     */
    public suspend fun getInfo(): GetInfoReturn {
        val parameter = null
        val result = cdp.callCommand("SystemInfo.getInfo", parameter)
        return result!!.let { Serialization.json.decodeFromJsonElement(it) }
    }

    /**
     * Returns information about the feature state.
     */
    public suspend fun getFeatureState(args: GetFeatureStateParameter): GetFeatureStateReturn {
        val parameter = Serialization.json.encodeToJsonElement(args)
        val result = cdp.callCommand("SystemInfo.getFeatureState", parameter)
        return result!!.let { Serialization.json.decodeFromJsonElement(it) }
    }

    /**
     * Returns information about the feature state.
     *
     * @param featureState No description
     */
    public suspend fun getFeatureState(featureState: String): GetFeatureStateReturn {
        val parameter = GetFeatureStateParameter(featureState = featureState)
        return getFeatureState(parameter)
    }

    /**
     * Returns information about all running processes.
     */
    public suspend fun getProcessInfo(): GetProcessInfoReturn {
        val parameter = null
        val result = cdp.callCommand("SystemInfo.getProcessInfo", parameter)
        return result!!.let { Serialization.json.decodeFromJsonElement(it) }
    }

    /**
     * Describes a single graphics processor (GPU).
     */
    @Serializable
    public data class GPUDevice(
        /**
         * PCI ID of the GPU vendor, if available; 0 otherwise.
         */
        public val vendorId: Double,
        /**
         * PCI ID of the GPU device, if available; 0 otherwise.
         */
        public val deviceId: Double,
        /**
         * Sub sys ID of the GPU, only available on Windows.
         */
        public val subSysId: Double? = null,
        /**
         * Revision of the GPU, only available on Windows.
         */
        public val revision: Double? = null,
        /**
         * String description of the GPU vendor, if the PCI ID is not available.
         */
        public val vendorString: String,
        /**
         * String description of the GPU device, if the PCI ID is not available.
         */
        public val deviceString: String,
        /**
         * String description of the GPU driver vendor.
         */
        public val driverVendor: String,
        /**
         * String description of the GPU driver version.
         */
        public val driverVersion: String,
    )

    /**
     * Describes the width and height dimensions of an entity.
     */
    @Serializable
    public data class Size(
        /**
         * Width in pixels.
         */
        public val width: Int,
        /**
         * Height in pixels.
         */
        public val height: Int,
    )

    /**
     * Describes a supported video decoding profile with its associated minimum and
     * maximum resolutions.
     */
    @Serializable
    public data class VideoDecodeAcceleratorCapability(
        /**
         * Video codec profile that is supported, e.g. VP9 Profile 2.
         */
        public val profile: String,
        /**
         * Maximum video dimensions in pixels supported for this |profile|.
         */
        public val maxResolution: Size,
        /**
         * Minimum video dimensions in pixels supported for this |profile|.
         */
        public val minResolution: Size,
    )

    /**
     * Describes a supported video encoding profile with its associated maximum
     * resolution and maximum framerate.
     */
    @Serializable
    public data class VideoEncodeAcceleratorCapability(
        /**
         * Video codec profile that is supported, e.g H264 Main.
         */
        public val profile: String,
        /**
         * Maximum video dimensions in pixels supported for this |profile|.
         */
        public val maxResolution: Size,
        /**
         * Maximum encoding framerate in frames per second supported for this
         * |profile|, as fraction's numerator and denominator, e.g. 24/1 fps,
         * 24000/1001 fps, etc.
         */
        public val maxFramerateNumerator: Int,
        public val maxFramerateDenominator: Int,
    )

    /**
     * YUV subsampling type of the pixels of a given image.
     */
    @Serializable
    public enum class SubsamplingFormat {
        @SerialName("yuv420")
        YUV420,
        @SerialName("yuv422")
        YUV422,
        @SerialName("yuv444")
        YUV444,
    }

    /**
     * Image format of a given image.
     */
    @Serializable
    public enum class ImageType {
        @SerialName("jpeg")
        JPEG,
        @SerialName("webp")
        WEBP,
        @SerialName("unknown")
        UNKNOWN,
    }

    /**
     * Describes a supported image decoding profile with its associated minimum and
     * maximum resolutions and subsampling.
     */
    @Serializable
    public data class ImageDecodeAcceleratorCapability(
        /**
         * Image coded, e.g. Jpeg.
         */
        public val imageType: ImageType,
        /**
         * Maximum supported dimensions of the image in pixels.
         */
        public val maxDimensions: Size,
        /**
         * Minimum supported dimensions of the image in pixels.
         */
        public val minDimensions: Size,
        /**
         * Optional array of supported subsampling formats, e.g. 4:2:0, if known.
         */
        public val subsamplings: List<SubsamplingFormat>,
    )

    /**
     * Provides information about the GPU(s) on the system.
     */
    @Serializable
    public data class GPUInfo(
        /**
         * The graphics devices on the system. Element 0 is the primary GPU.
         */
        public val devices: List<GPUDevice>,
        /**
         * An optional dictionary of additional GPU related attributes.
         */
        public val auxAttributes: Map<String, JsonElement>? = null,
        /**
         * An optional dictionary of graphics features and their status.
         */
        public val featureStatus: Map<String, JsonElement>? = null,
        /**
         * An optional array of GPU driver bug workarounds.
         */
        public val driverBugWorkarounds: List<String>,
        /**
         * Supported accelerated video decoding capabilities.
         */
        public val videoDecoding: List<VideoDecodeAcceleratorCapability>,
        /**
         * Supported accelerated video encoding capabilities.
         */
        public val videoEncoding: List<VideoEncodeAcceleratorCapability>,
        /**
         * Supported accelerated image decoding capabilities.
         */
        public val imageDecoding: List<ImageDecodeAcceleratorCapability>,
    )

    /**
     * Represents process info.
     */
    @Serializable
    public data class ProcessInfo(
        /**
         * Specifies process type.
         */
        public val type: String,
        /**
         * Specifies process id.
         */
        public val id: Int,
        /**
         * Specifies cumulative CPU usage in seconds across all threads of the
         * process since the process start.
         */
        public val cpuTime: Double,
    )

    @Serializable
    public data class GetInfoReturn(
        /**
         * Information about the GPUs on the system.
         */
        public val gpu: GPUInfo,
        /**
         * A platform-dependent description of the model of the machine. On Mac OS, this is, for
         * example, 'MacBookPro'. Will be the empty string if not supported.
         */
        public val modelName: String,
        /**
         * A platform-dependent description of the version of the machine. On Mac OS, this is, for
         * example, '10.1'. Will be the empty string if not supported.
         */
        public val modelVersion: String,
        /**
         * The command line string used to launch the browser. Will be the empty string if not
         * supported.
         */
        public val commandLine: String,
    )

    @Serializable
    public data class GetFeatureStateParameter(
        public val featureState: String,
    )

    @Serializable
    public data class GetFeatureStateReturn(
        public val featureEnabled: Boolean,
    )

    @Serializable
    public data class GetProcessInfoReturn(
        /**
         * An array of process info blocks.
         */
        public val processInfo: List<ProcessInfo>,
    )
}
