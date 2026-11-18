package dev.kdriver.core.browser

import kotlinx.cinterop.*
import kotlinx.io.files.Path
import platform.posix.*

@OptIn(ExperimentalForeignApi::class)
internal actual fun deleteRecursively(path: Path): Boolean {
    val target = path.toString()
    deleteEntry(target)
    return !entryExists(target)
}

/**
 * Deletes [path], and its content first if it is a directory. A symbolic link is unlinked, never
 * followed: `lstat` describes the link itself.
 */
@OptIn(ExperimentalForeignApi::class)
private fun deleteEntry(path: String) {
    if (isRealDirectory(path)) {
        children(path).forEach { deleteEntry("$path/$it") }
        rmdir(path)
    } else {
        unlink(path)
    }
}

/** Lists the names in [path], read in full before anything is deleted from it. */
@OptIn(ExperimentalForeignApi::class)
private fun children(path: String): List<String> {
    val dir = opendir(path) ?: return emptyList()
    val names = mutableListOf<String>()
    try {
        while (true) {
            val entry = readdir(dir) ?: break
            val name = entry.pointed.d_name.toKString()
            if (name != "." && name != "..") names += name
        }
    } finally {
        closedir(dir)
    }
    return names
}

@OptIn(ExperimentalForeignApi::class, UnsafeNumber::class)
private fun isRealDirectory(path: String): Boolean = memScoped {
    val info = alloc<stat>()
    lstat(path, info.ptr) == 0 && (info.st_mode.convert<Int>() and S_IFMT.convert<Int>()) == S_IFDIR.convert<Int>()
}

@OptIn(ExperimentalForeignApi::class)
private fun entryExists(path: String): Boolean = memScoped {
    val info = alloc<stat>()
    lstat(path, info.ptr) == 0
}
