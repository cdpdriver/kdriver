package dev.kdriver.core.browser

import kotlinx.cinterop.*
import kotlinx.io.files.Path
import platform.windows.*

@OptIn(ExperimentalForeignApi::class)
internal actual fun deleteRecursively(path: Path): Boolean {
    val target = path.toString()
    deleteEntry(target)
    return GetFileAttributesW(target) == INVALID_FILE_ATTRIBUTES
}

/**
 * Deletes [path], and its content first if it is a directory. A junction or symbolic link (a reparse
 * point) is removed itself, never followed.
 */
@OptIn(ExperimentalForeignApi::class)
private fun deleteEntry(path: String) {
    val attributes = GetFileAttributesW(path)
    if (attributes == INVALID_FILE_ATTRIBUTES) return
    // DeleteFileW and RemoveDirectoryW both refuse a read-only entry.
    val readOnly = FILE_ATTRIBUTE_READONLY.toUInt()
    if (attributes and readOnly != 0u) SetFileAttributesW(path, attributes and readOnly.inv())

    val isDirectory = attributes and FILE_ATTRIBUTE_DIRECTORY.toUInt() != 0u
    val isLink = attributes and FILE_ATTRIBUTE_REPARSE_POINT.toUInt() != 0u
    when {
        isDirectory && !isLink -> {
            children(path).forEach { deleteEntry("$path\\$it") }
            RemoveDirectoryW(path)
        }

        isDirectory -> RemoveDirectoryW(path)
        else -> DeleteFileW(path)
    }
}

/** Lists the names in [path], read in full before anything is deleted from it. */
@OptIn(ExperimentalForeignApi::class)
private fun children(path: String): List<String> = memScoped {
    val data = alloc<WIN32_FIND_DATAW>()
    val handle = FindFirstFileW("$path\\*", data.ptr)
    if (handle == null || handle == INVALID_HANDLE_VALUE) return@memScoped emptyList()
    val names = mutableListOf<String>()
    try {
        do {
            val name = data.cFileName.toKStringFromUtf16()
            if (name != "." && name != "..") names += name
        } while (FindNextFileW(handle, data.ptr) != 0)
    } finally {
        FindClose(handle)
    }
    names
}
