package dev.kdriver.core.browser

import kotlinx.io.files.Path
import java.nio.file.LinkOption
import java.nio.file.Paths
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.deleteRecursively
import kotlin.io.path.exists

/**
 * `Path.deleteRecursively` does not follow symbolic links, and keeps going past an entry it cannot
 * delete before throwing: the end state is what tells whether the profile is gone.
 */
@OptIn(ExperimentalPathApi::class)
internal actual fun deleteRecursively(path: Path): Boolean {
    val target = Paths.get(path.toString())
    try {
        target.deleteRecursively()
    } catch (_: Exception) {
        // Reported below, through whether the directory is still there.
    }
    return !target.exists(LinkOption.NOFOLLOW_LINKS)
}
