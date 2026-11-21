package dev.kdriver.core.browser

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.UnsafeNumber
import kotlinx.cinterop.convert
import platform.posix.*

/**
 * Kotlin/Native exposes none of the `*at` functions on Linux, so they go through `/proc/self/fd`:
 * `/proc/self/fd/<parent>/<name>` resolves `<parent>` to exactly the directory that descriptor is
 * open on, then looks `<name>` up in it, which is what `openat`, `unlinkat` and `fchmodat` do (glibc
 * implements `fchmodat` this way too).
 *
 * Without `/proc` (some minimal containers), the entry's full path is used instead. A path is only
 * as safe as its parents then; the profile directory is created with owner-only permissions, so
 * only its owner could change them during the walk.
 */
@OptIn(ExperimentalForeignApi::class, UnsafeNumber::class)
internal actual object DirectoryAccess {

    actual fun openDirectory(entry: Entry): Int =
        open(entry.resolved(), O_RDONLY or O_DIRECTORY or O_NOFOLLOW or O_CLOEXEC)

    actual fun grantOwnerAccess(entry: Entry): Boolean {
        // A descriptor that only designates the directory (O_PATH needs no permission on it); with
        // O_NOFOLLOW and O_DIRECTORY, a link that replaced it is refused. Its /proc link then names
        // exactly that directory, whatever happens to the path.
        val fd = open(entry.resolved(), O_PATH or O_DIRECTORY or O_NOFOLLOW or O_CLOEXEC)
        if (fd < 0) return false
        return try {
            chmod("$PROC_FDS/$fd", S_IRWXU.convert()) == 0
        } finally {
            close(fd)
        }
    }

    actual fun remove(entry: Entry, directory: Boolean) {
        // unlink and rmdir never follow a link in place of the last component.
        if (directory) rmdir(entry.resolved()) else unlink(entry.resolved())
    }

    private fun Entry.resolved(): String =
        if (parentFd == NO_PARENT || !procAvailable) path else "$PROC_FDS/$parentFd/$name"

    private val procAvailable: Boolean by lazy { access("$PROC_FDS/", F_OK) == 0 }

    private const val PROC_FDS = "/proc/self/fd"

    /** From the kernel's `fcntl.h`, the same on x86-64 and arm64; glibc's headers hide it here. */
    private const val O_PATH = 0x200000

}
