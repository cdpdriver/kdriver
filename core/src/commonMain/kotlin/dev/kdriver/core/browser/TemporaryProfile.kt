package dev.kdriver.core.browser

import kotlinx.coroutines.delay
import kotlinx.io.files.Path

/**
 * Deletes [path] and everything below it, without following symbolic links (or, on Windows,
 * junctions): a link inside the profile is removed, never what it points to.
 *
 * Best effort: keeps going past an entry it cannot delete, so one locked file does not keep the
 * rest of the profile on disk.
 *
 * @return true if [path] no longer exists afterwards (including when it never existed).
 */
internal expect fun deleteRecursively(path: Path): Boolean

/**
 * Deletes a temporary profile, retrying for a short while if it cannot be removed at once.
 *
 * Even after the browser process has exited, a file of the profile can stay locked for a moment
 * (on Windows by a child process that is still winding down, or by an antivirus scanning it), so a
 * single attempt is not enough.
 *
 * @param path the profile directory.
 * @param attempts how many times to try at most.
 * @param retryDelayMillis how long to wait between two attempts.
 * @param delete one attempt, [deleteRecursively] outside of tests.
 * @return true if the profile is gone.
 */
internal suspend fun deleteTemporaryProfile(
    path: Path,
    attempts: Int = PROFILE_DELETE_ATTEMPTS,
    retryDelayMillis: Long = PROFILE_DELETE_RETRY_DELAY_MILLIS,
    delete: (Path) -> Boolean = ::deleteRecursively,
): Boolean {
    repeat(attempts) { attempt ->
        if (delete(path)) return true
        if (attempt < attempts - 1) delay(retryDelayMillis)
    }
    return false
}

private const val PROFILE_DELETE_ATTEMPTS = 10
private const val PROFILE_DELETE_RETRY_DELAY_MILLIS = 200L
