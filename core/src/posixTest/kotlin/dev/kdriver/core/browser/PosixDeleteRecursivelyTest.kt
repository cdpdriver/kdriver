package dev.kdriver.core.browser

import kotlinx.cinterop.*
import kotlinx.io.files.Path
import platform.posix.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/** The POSIX recursive delete of a temporary profile, on the native targets. */
@OptIn(ExperimentalForeignApi::class, UnsafeNumber::class)
class PosixDeleteRecursivelyTest {

    /** Directories and files at several levels are all deleted. */
    @Test
    fun removesNestedContent() {
        val root = tempDir("kdriver_delete_test_")
        makeDir("$root/Default")
        makeDir("$root/Default/Cache")
        writeFile("$root/Default/Cache/data")
        writeFile("$root/Local State")

        assertTrue(deleteRecursively(Path(root)))
        assertFalse(exists(root))
    }

    /** A directory of the profile without read, or without write and search permission, is made deletable. */
    @Test
    fun removesADirectoryItCannotListAtFirst() {
        // A permission check that root bypasses: nothing to prove there.
        if (getuid() == 0u) return
        val root = tempDir("kdriver_delete_test_")
        makeDir("$root/locked")
        writeFile("$root/locked/file")
        chmod("$root/locked", 0.convert())
        makeDir("$root/searchOnly")
        writeFile("$root/searchOnly/file")
        chmod("$root/searchOnly", S_IRUSR.convert())

        assertTrue(deleteRecursively(Path(root)), "an entry of our own profile is made deletable first")
        assertFalse(exists(root))
    }

    /** A link inside the profile is unlinked: its target is neither deleted nor has its mode changed. */
    @Test
    fun neverFollowsNorChangesWhatALinkPointsTo() {
        val outside = tempDir("kdriver_outside_test_")
        writeFile("$outside/kept")
        // Read-only, so a delete that followed the link would have to chmod it first.
        chmod(outside, (S_IRUSR or S_IXUSR).convert())
        val root = tempDir("kdriver_delete_test_")
        try {
            assertEquals(0, symlink(outside, "$root/link"))

            assertTrue(deleteRecursively(Path(root)))

            assertFalse(exists(root))
            assertTrue(exists("$outside/kept"), "what a link points to must never be deleted")
            assertEquals(S_IRUSR or S_IXUSR, modeOf(outside) and S_IRWXU, "nor have its mode changed")
        } finally {
            chmod(outside, S_IRWXU.convert())
            deleteRecursively(Path(outside))
        }
    }

    /** A profile path that is itself a link is unlinked, not followed. */
    @Test
    fun removesARootThatIsALinkWithoutFollowingIt() {
        val outside = tempDir("kdriver_outside_test_")
        writeFile("$outside/kept")
        val link = "${tempDir("kdriver_link_parent_")}/profile"
        try {
            assertEquals(0, symlink(outside, link))

            assertTrue(deleteRecursively(Path(link)))

            assertFalse(exists(link))
            assertTrue(exists("$outside/kept"))
        } finally {
            deleteRecursively(Path(outside))
            deleteRecursively(Path(link.substringBeforeLast('/')))
        }
    }

    /** Nothing to delete counts as deleted. */
    @Test
    fun isTrueForAMissingDirectory() {
        val missing = tempDir("kdriver_missing_test_").also { rmdir(it) }

        assertTrue(deleteRecursively(Path(missing)))
    }

    private fun tempDir(prefix: String): String {
        val base = getenv("TMPDIR")?.toKString()?.trimEnd('/')?.takeIf { it.isNotEmpty() } ?: "/tmp"
        val template = "$base/${prefix}XXXXXX".cstr
        return memScoped {
            val buffer = template.getPointer(this)
            mkdtemp(buffer)?.toKString() ?: error("mkdtemp failed: errno $errno")
        }
    }

    private fun makeDir(path: String) {
        assertEquals(0, mkdir(path, S_IRWXU.convert()), "mkdir $path")
    }

    private fun writeFile(path: String) {
        val file = fopen(path, "w") ?: error("fopen $path failed: errno $errno")
        fputs("x", file)
        fclose(file)
    }

    private fun exists(path: String): Boolean = memScoped {
        lstat(path, alloc<stat>().ptr) == 0
    }

    private fun modeOf(path: String): Int = memScoped {
        val info = alloc<stat>()
        check(lstat(path, info.ptr) == 0)
        info.st_mode.convert()
    }

}
