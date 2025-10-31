package dev.kdriver.core.extensions

import dev.kdriver.core.browser.Config
import dev.kdriver.core.browser.createBrowser
import dev.kdriver.core.sampleFile
import kotlinx.coroutines.runBlocking
import kotlinx.io.files.Path
import kotlin.io.path.createTempDirectory
import kotlin.io.path.writeText
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class ExtensionsTest {

    @Test
    fun testGraphemeClusters() {
        val text = "Hello World"
        val clusters = text.graphemeClusters()
        assertEquals(11, clusters.size)
        assertEquals("H", clusters[0])
        assertEquals(" ", clusters[5])
        assertEquals("d", clusters[10])
    }

    @Test
    fun testGraphemeClustersWithEmojis() {
        val text = "Hello 👋 World"
        val clusters = text.graphemeClusters()
        assertTrue(clusters.contains("👋"))
    }

    @Test
    fun testGraphemeClustersWithComplexChars() {
        val text = "Café"
        val clusters = text.graphemeClusters()
        assertEquals(4, clusters.size)
    }

    @Test
    fun testSendKeysWithSpecialChars() = runBlocking {
        val browser = createBrowser(this, headless = true, sandbox = false)
        val tab = browser.get(sampleFile("groceries.html"))

        val input = tab.select("#my_input")
        input.sendKeysWithSpecialChars("Hello 👋")

        val value = input.getInputValue()
        assertTrue(value?.contains("Hello") == true)
        assertTrue(value?.contains("👋") == true)

        browser.stop()
    }

    @Test
    fun testAddExtensionWithNonExistentPath() {
        val config = Config(browserExecutablePath = null)
        val nonExistentPath = Path("/non/existent/path")

        assertFailsWith<kotlinx.io.files.FileNotFoundException> {
            config.addExtension(nonExistentPath)
        }
    }

    @Test
    fun testAddExtensionWithDirectoryMissingManifest() {
        val config = Config(browserExecutablePath = null)
        val tempDir = createTempDirectory(prefix = "test_ext_")
        val tempDirPath = Path(tempDir.toString())

        assertFailsWith<kotlinx.io.files.FileNotFoundException> {
            config.addExtension(tempDirPath)
        }
    }

    @Test
    fun testAddExtensionWithDirectoryWithManifest() {
        val config = Config(browserExecutablePath = null)
        val tempDir = createTempDirectory(prefix = "test_ext_")
        val manifestFile = kotlin.io.path.Path(tempDir.toString(), "manifest.json")
        manifestFile.writeText("{\"name\": \"test\"}")

        val tempDirPath = Path(tempDir.toString())
        config.addExtension(tempDirPath)

        assertTrue(config._extensions.isNotEmpty())
    }

}
