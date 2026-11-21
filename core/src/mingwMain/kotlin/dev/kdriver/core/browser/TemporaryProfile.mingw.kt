package dev.kdriver.core.browser

import kotlinx.cinterop.*
import kotlinx.io.files.Path
import platform.windows.*

/**
 * Every path goes through [extendedLengthPath]: a profile nests deep enough (extension and cache
 * directories) to pass the 260 characters a plain path is limited to, and such entries would be left.
 */
@OptIn(ExperimentalForeignApi::class)
internal actual fun deleteRecursively(path: Path): Boolean {
    val target = extendedLengthPath(path.toString())
    deleteEntry(target)
    return isGone(target)
}

/** Only when the system says the entry does not exist: an invalid path or a denied access is not gone. */
@OptIn(ExperimentalForeignApi::class)
private fun isGone(path: String): Boolean {
    if (GetFileAttributesW(path) != INVALID_FILE_ATTRIBUTES) return false
    val error = GetLastError()
    return error == ERROR_FILE_NOT_FOUND.toUInt() || error == ERROR_PATH_NOT_FOUND.toUInt()
}

/**
 * [path] in the `\\?\` form, which the wide Windows file functions accept beyond `MAX_PATH` (260
 * characters) without any system setting. That form is taken literally, with no normalisation, so
 * separators are made single backslashes first. A relative path cannot take it and is returned as is
 * (a temporary profile is always absolute).
 */
internal fun extendedLengthPath(path: String): String {
    val slashes = path.replace('/', '\\')
    // A leading pair is the start of a UNC, extended or device path: kept, the rest collapsed.
    val lead = if (slashes.startsWith("\\\\")) "\\\\" else ""
    val windowsPath = lead + collapseSeparators(slashes.substring(lead.length))
    return when {
        windowsPath.startsWith(EXTENDED_PREFIX) || windowsPath.startsWith(DEVICE_PREFIX) -> windowsPath
        // \\server\share\... becomes \\?\UNC\server\share\...
        windowsPath.startsWith("\\\\") -> EXTENDED_PREFIX + "UNC\\" + windowsPath.removePrefix("\\\\")
        isDriveAbsolute(windowsPath) -> EXTENDED_PREFIX + windowsPath
        else -> windowsPath
    }
}

private const val EXTENDED_PREFIX = "\\\\?\\"
private const val DEVICE_PREFIX = "\\\\.\\"

/** `C:\\...`: a drive letter, a colon and a separator. */
private fun isDriveAbsolute(path: String): Boolean =
    path.getOrNull(0)?.isLetter() == true && path.getOrNull(1) == ':' && path.getOrNull(2) == '\\'

private fun collapseSeparators(path: String): String = buildString(path.length) {
    path.forEach { c -> if (c != '\\' || lastOrNull() != '\\') append(c) }
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
