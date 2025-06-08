package dev.kdriver.cdp.domain

import dev.kaccelero.serializers.Serialization
import dev.kdriver.cdp.CDP
import dev.kdriver.cdp.Domain
import dev.kdriver.cdp.cacheGeneratedDomain
import dev.kdriver.cdp.getGeneratedDomain
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.encodeToJsonElement

public val CDP.io: IO
    get() = getGeneratedDomain() ?: cacheGeneratedDomain(IO(this))

/**
 * Input/Output operations for streams produced by DevTools.
 */
public class IO(
    private val cdp: CDP,
) : Domain {
    /**
     * Close the stream, discard any temporary backing storage.
     */
    public suspend fun close(args: CloseParameter) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("IO.close", parameter)
    }

    /**
     * Close the stream, discard any temporary backing storage.
     *
     * @param handle Handle of the stream to close.
     */
    public suspend fun close(handle: String) {
        val parameter = CloseParameter(handle = handle)
        close(parameter)
    }

    /**
     * Read a chunk of the stream
     */
    public suspend fun read(args: ReadParameter): ReadReturn {
        val parameter = Serialization.json.encodeToJsonElement(args)
        val result = cdp.callCommand("IO.read", parameter)
        return result!!.let { Serialization.json.decodeFromJsonElement(it) }
    }

    /**
     * Read a chunk of the stream
     *
     * @param handle Handle of the stream to read.
     * @param offset Seek to the specified offset before reading (if not specificed, proceed with offset
     * following the last read). Some types of streams may only support sequential reads.
     * @param size Maximum number of bytes to read (left upon the agent discretion if not specified).
     */
    public suspend fun read(
        handle: String,
        offset: Int? = null,
        size: Int? = null,
    ): ReadReturn {
        val parameter = ReadParameter(handle = handle, offset = offset, size = size)
        return read(parameter)
    }

    /**
     * Return UUID of Blob object specified by a remote object id.
     */
    public suspend fun resolveBlob(args: ResolveBlobParameter): ResolveBlobReturn {
        val parameter = Serialization.json.encodeToJsonElement(args)
        val result = cdp.callCommand("IO.resolveBlob", parameter)
        return result!!.let { Serialization.json.decodeFromJsonElement(it) }
    }

    /**
     * Return UUID of Blob object specified by a remote object id.
     *
     * @param objectId Object id of a Blob object wrapper.
     */
    public suspend fun resolveBlob(objectId: String): ResolveBlobReturn {
        val parameter = ResolveBlobParameter(objectId = objectId)
        return resolveBlob(parameter)
    }

    @Serializable
    public data class CloseParameter(
        /**
         * Handle of the stream to close.
         */
        public val handle: String,
    )

    @Serializable
    public data class ReadParameter(
        /**
         * Handle of the stream to read.
         */
        public val handle: String,
        /**
         * Seek to the specified offset before reading (if not specificed, proceed with offset
         * following the last read). Some types of streams may only support sequential reads.
         */
        public val offset: Int? = null,
        /**
         * Maximum number of bytes to read (left upon the agent discretion if not specified).
         */
        public val size: Int? = null,
    )

    @Serializable
    public data class ReadReturn(
        /**
         * Set if the data is base64-encoded
         */
        public val base64Encoded: Boolean?,
        /**
         * Data that were read.
         */
        public val `data`: String,
        /**
         * Set if the end-of-file condition occurred while reading.
         */
        public val eof: Boolean,
    )

    @Serializable
    public data class ResolveBlobParameter(
        /**
         * Object id of a Blob object wrapper.
         */
        public val objectId: String,
    )

    @Serializable
    public data class ResolveBlobReturn(
        /**
         * UUID of the specified Blob.
         */
        public val uuid: String,
    )
}
