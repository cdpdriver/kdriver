// One actual per POSIX target, named after the expect object it implements.
@file:Suppress("MatchingDeclarationName")

package dev.kdriver.core.browser

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.UnsafeNumber
import kotlinx.cinterop.convert
import platform.posix.*

/**
 * The `*at` functions, except `openat`, which Kotlin/Native does not expose on macOS: a directory is
 * opened through its whole path instead, with `O_NOFOLLOW_ANY`, which refuses a symbolic link in any
 * component of the path and so keeps the walk inside the profile just as well. The walk starts from a
 * path whose parent is canonical, free of links (`$TMPDIR` itself is under `/var`, a link).
 *
 * `O_NOFOLLOW_ANY` alone: it already covers the last component, and macOS refuses it combined with
 * `O_NOFOLLOW` (`EINVAL`).
 */
@OptIn(ExperimentalForeignApi::class, UnsafeNumber::class)
internal actual object DirectoryAccess {

    actual fun openDirectory(entry: Entry): Int =
        open(entry.path, O_RDONLY or O_DIRECTORY or O_NOFOLLOW_ANY or O_CLOEXEC)

    actual fun grantOwnerAccess(entry: Entry): Boolean =
        fchmodat(entry.dirFd(), entry.name, S_IRWXU.convert(), AT_SYMLINK_NOFOLLOW) == 0

    actual fun remove(entry: Entry, directory: Boolean) {
        unlinkat(entry.dirFd(), entry.name, if (directory) AT_REMOVEDIR else 0)
    }

    private fun Entry.dirFd(): Int = if (parentFd == NO_PARENT) AT_FDCWD else parentFd

}
