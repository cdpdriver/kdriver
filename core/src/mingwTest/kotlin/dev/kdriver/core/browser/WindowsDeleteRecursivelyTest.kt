package dev.kdriver.core.browser

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.toKString
import kotlinx.io.files.Path
import platform.posix.getenv
import platform.windows.*
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

/** The Windows recursive delete of a temporary profile. */
@OptIn(ExperimentalForeignApi::class)
class WindowsDeleteRecursivelyTest {

    /** Entries nested past `MAX_PATH` are deleted, through the extended-length form. */
    @Test
    fun removesEntriesBeyondTheMaxPathLimit() {
        val root = "${tempBase()}\\kdriver_long_path_test_${randomSuffix()}"
        // Nested well past 260 characters, as a profile's extension or cache directories can be.
        var deepest = root
        createDirectory(root)
        repeat(LEVELS) { level ->
            deepest = "$deepest\\${"d$level".padEnd(LEVEL_NAME_LENGTH, 'x')}"
            createDirectory(deepest)
        }
        assertTrue(deepest.length > MAX_PATH, "the test path must pass MAX_PATH, is ${deepest.length}")
        val file = "$deepest\\Cookies"
        val handle: HANDLE? = CreateFileW(
            extendedLengthPath(file),
            GENERIC_WRITE.toUInt(),
            0u,
            null,
            CREATE_NEW.toUInt(),
            FILE_ATTRIBUTE_NORMAL.toUInt(),
            null,
        )
        assertTrue(handle != null && handle != INVALID_HANDLE_VALUE, "create $file, error ${GetLastError()}")
        CloseHandle(handle)

        assertTrue(deleteRecursively(Path(root)), "every entry is deleted, however deep")
        assertEquals(INVALID_FILE_ATTRIBUTES, GetFileAttributesW(extendedLengthPath(root)))
    }

    /** The extended-length form of drive, UNC, already extended, device and relative paths. */
    @Test
    fun extendedLengthPathPrefixesAbsolutePathsOnly() {
        assertEquals("\\\\?\\C:\\Temp\\kdriver_1", extendedLengthPath("C:\\Temp\\kdriver_1"))
        assertEquals("\\\\?\\C:\\Temp\\kdriver_1", extendedLengthPath("C:/Temp/kdriver_1"))
        assertEquals("\\\\?\\UNC\\server\\share\\kdriver_1", extendedLengthPath("\\\\server\\share\\kdriver_1"))
        assertEquals("\\\\?\\C:\\already", extendedLengthPath("\\\\?\\C:\\already"))
        assertEquals("\\\\.\\device", extendedLengthPath("\\\\.\\device"))
        assertEquals("relative\\kdriver_1", extendedLengthPath("relative/kdriver_1"))
        // The extended form is taken literally: a doubled separator (TEMP ending with one) would break it.
        assertEquals("\\\\?\\C:\\Temp\\kdriver_1", extendedLengthPath("C:\\Temp\\\\kdriver_1"))
        assertEquals("\\\\?\\UNC\\server\\share\\kdriver_1", extendedLengthPath("\\\\server\\share\\\\kdriver_1"))
    }

    /** A doubled separator, which the extended-length form would take literally, is collapsed. */
    @Test
    fun removesAProfileWhosePathHasADoubledSeparator() {
        val name = "kdriver_double_separator_test_${randomSuffix()}"
        createDirectory("${tempBase()}\\$name")
        createDirectory("${tempBase()}\\$name\\Default")

        assertTrue(deleteRecursively(Path("${tempBase()}\\\\$name")))
        assertEquals(INVALID_FILE_ATTRIBUTES, GetFileAttributesW(extendedLengthPath("${tempBase()}\\$name")))
    }

    /** Only a missing entry counts as deleted, not one the system refuses to reach. */
    @Test
    fun aProfileThatCannotBeReachedIsNotReportedGone() {
        // An invalid name, not a missing one: deleting it fails, and must not count as done.
        assertFalse(deleteRecursively(Path("C:\\kdriver_invalid_<name>")))
    }

    /** `TEMP` ending with a separator does not double it in the profile path. */
    @Test
    fun tempProfileDirHasNoDoubledSeparator() {
        assertFalse("\\\\" in tempProfileDir().toString().removePrefix("\\\\"))
    }

    private fun randomSuffix(): String = Random.nextLong().toString(HEX)

    private fun tempBase(): String =
        (getenv("TEMP") ?: getenv("TMP"))?.toKString()?.trimEnd('\\', '/') ?: "C:\\Temp"

    private fun createDirectory(path: String) {
        assertNotEquals(0, CreateDirectoryW(extendedLengthPath(path), null), "create $path, error ${GetLastError()}")
    }

    private companion object {
        /** Eight levels of 40 characters: well past the 260 of `MAX_PATH`. */
        const val LEVELS = 8
        const val LEVEL_NAME_LENGTH = 40
        const val MAX_PATH = 260
        const val HEX = 16
    }

}
