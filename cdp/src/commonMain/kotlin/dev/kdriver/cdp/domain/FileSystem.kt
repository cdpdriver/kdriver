@file:Suppress("ALL")

package dev.kdriver.cdp.domain

import dev.kdriver.cdp.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.encodeToJsonElement

public val CDP.fileSystem: FileSystem
    get() = getGeneratedDomain() ?: cacheGeneratedDomain(FileSystem(this))

public class FileSystem(
    private val cdp: CDP,
) : Domain {
    public suspend fun getDirectory(
        args: GetDirectoryParameter,
        mode: CommandMode = CommandMode.DEFAULT,
    ): GetDirectoryReturn {
        val parameter = Serialization.json.encodeToJsonElement(args)
        val result = cdp.callCommand("FileSystem.getDirectory", parameter, mode)
        return result!!.let { Serialization.json.decodeFromJsonElement(it) }
    }

    /**
     *
     *
     * @param bucketFileSystemLocator No description
     */
    public suspend fun getDirectory(bucketFileSystemLocator: BucketFileSystemLocator): GetDirectoryReturn {
        val parameter = GetDirectoryParameter(bucketFileSystemLocator = bucketFileSystemLocator)
        return getDirectory(parameter)
    }

    @Serializable
    public data class File(
        public val name: String,
        /**
         * Timestamp
         */
        public val lastModified: Double,
        /**
         * Size in bytes
         */
        public val size: Double,
        public val type: String,
    )

    @Serializable
    public data class Directory(
        public val name: String,
        public val nestedDirectories: List<String>,
        /**
         * Files that are directly nested under this directory.
         */
        public val nestedFiles: List<File>,
    )

    @Serializable
    public data class BucketFileSystemLocator(
        /**
         * Storage key
         */
        public val storageKey: String,
        /**
         * Bucket name. Not passing a `bucketName` will retrieve the default Bucket. (https://developer.mozilla.org/en-US/docs/Web/API/Storage_API#storage_buckets)
         */
        public val bucketName: String? = null,
        /**
         * Path to the directory using each path component as an array item.
         */
        public val pathComponents: List<String>,
    )

    @Serializable
    public data class GetDirectoryParameter(
        public val bucketFileSystemLocator: BucketFileSystemLocator,
    )

    @Serializable
    public data class GetDirectoryReturn(
        /**
         * Returns the directory object at the path.
         */
        public val directory: Directory,
    )
}
