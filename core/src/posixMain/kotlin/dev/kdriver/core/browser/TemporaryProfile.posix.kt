package dev.kdriver.core.browser

import kotlinx.cinterop.*
import kotlinx.io.files.Path
import platform.posix.*

/**
 * Every entry is reached from its parent's open descriptor, never through a path that could be
 * changed during the walk (see [DirectoryAccess]): an entry replaced by a symbolic link between two
 * steps is unlinked, never followed, so nothing outside the profile is ever deleted, listed, or has
 * its mode changed.
 */
@OptIn(ExperimentalForeignApi::class)
internal actual fun deleteRecursively(path: Path): Boolean {
    val target = path.toString().trimEnd('/').ifEmpty { "/" }
    val root = withCanonicalParent(target)
    deleteEntry(Entry(NO_PARENT, root, root))
    return !entryExists(target)
}

/**
 * [path] with its parent directory in canonical form, free of symbolic links (on macOS `$TMPDIR` is
 * under `/var`, itself a link), so that a link met during the walk can only be one that appeared
 * inside the profile. The last component is kept as is: a profile that is itself a link is unlinked,
 * not followed.
 */
@OptIn(ExperimentalForeignApi::class)
private fun withCanonicalParent(path: String): String {
    val slash = path.lastIndexOf('/')
    if (slash < 0) return path
    val parent = if (slash == 0) "/" else path.substring(0, slash)
    // A buffer of our own: the realpath Kotlin/Native binds on macOS is the legacy one, which refuses
    // a null buffer.
    val canonical = memScoped {
        val buffer = allocArray<ByteVar>(PATH_MAX + 1)
        realpath(parent, buffer)?.toKString()
    } ?: return path
    return "${canonical.trimEnd('/')}/${path.substring(slash + 1)}"
}

/**
 * An entry of the walk: [name] in the directory open as [parentFd], or the root [path] itself when
 * [parentFd] is [NO_PARENT]. [path] is the same entry from the root, for messages and for the
 * platforms without `*at` functions.
 */
internal class Entry(val parentFd: Int, val name: String, val path: String)

/** [Entry.parentFd] of the root of the walk, which is reached through its path. */
internal const val NO_PARENT = -1

/**
 * The `*at` operations the walk needs, which each POSIX target provides its own way: none of them
 * follows a symbolic link in place of [Entry.name], nor resolves [Entry.name] anywhere but in the
 * directory open as [Entry.parentFd].
 */
internal expect object DirectoryAccess {
    /** Opens [entry] if it is a real directory, or returns -1 with `errno` set. */
    fun openDirectory(entry: Entry): Int

    /** Gives the owner read, write and search permission on the directory [entry]. */
    fun grantOwnerAccess(entry: Entry): Boolean

    /** Removes [entry]: an empty directory if [directory], else a file or a link. */
    fun remove(entry: Entry, directory: Boolean)
}

/**
 * Deletes [entry], and its content first if it is a directory. Best effort: an entry that cannot be
 * deleted is left, and the walk goes on.
 */
@OptIn(ExperimentalForeignApi::class)
private fun deleteEntry(entry: Entry) {
    val dirFd = openDirectory(entry)
    if (dirFd < 0) {
        // Not a directory, a symbolic link (refused), or already gone: unlink it as it is.
        DirectoryAccess.remove(entry, directory = false)
        return
    }
    deleteChildren(dirFd, entry.path)
    DirectoryAccess.remove(entry, directory = true)
}

/**
 * Opens [entry] if it is a real directory (not a link to one), or returns -1.
 *
 * A directory without read permission cannot be opened: it is ours (inside the profile we created),
 * so we give ourselves access first, without following a link that may have replaced it since.
 */
@OptIn(ExperimentalForeignApi::class)
private fun openDirectory(entry: Entry): Int {
    val fd = DirectoryAccess.openDirectory(entry)
    if (fd >= 0 || errno != EACCES) return fd
    if (!DirectoryAccess.grantOwnerAccess(entry)) return -1
    return DirectoryAccess.openDirectory(entry)
}

/**
 * Deletes everything in the directory open as [dirFd], then closes it. The names are read in full
 * before anything is deleted.
 */
@OptIn(ExperimentalForeignApi::class, UnsafeNumber::class)
private fun deleteChildren(dirFd: Int, path: String) {
    // Emptying a directory needs write and search permission on it, on top of read to list it. The
    // mode is changed through the descriptor, so on the very directory that was opened.
    memScoped {
        val info = alloc<stat>()
        val ownerAccess = S_IRWXU.convert<Int>()
        if (fstat(dirFd, info.ptr) == 0 && (info.st_mode.convert<Int>() and ownerAccess) != ownerAccess) {
            fchmod(dirFd, (info.st_mode.convert<Int>() or ownerAccess).convert())
        }
    }
    // fdopendir takes over the descriptor: closedir closes it.
    val dir = fdopendir(dirFd) ?: run {
        close(dirFd)
        return
    }
    try {
        val names = mutableListOf<String>()
        while (true) {
            val entry = readdir(dir) ?: break
            val name = entry.pointed.d_name.toKString()
            if (name != "." && name != "..") names += name
        }
        names.forEach { deleteEntry(Entry(dirFd, it, "$path/$it")) }
    } finally {
        closedir(dir)
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun entryExists(path: String): Boolean = memScoped {
    val info = alloc<stat>()
    lstat(path, info.ptr) == 0
}
