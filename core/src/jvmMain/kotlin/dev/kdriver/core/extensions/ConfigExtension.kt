package dev.kdriver.core.extensions

import dev.kdriver.core.browser.Config
import kotlinx.io.files.FileNotFoundException
import kotlinx.io.files.Path
import java.io.File
import java.util.zip.ZipFile
import kotlin.io.copyTo
import kotlin.io.outputStream
import kotlin.io.path.createTempDirectory
import kotlin.io.walkTopDown
import kotlin.use

fun Config.addExtension(extensionPath: Path) {
    val file = File(extensionPath.toString())
    if (!file.exists()) {
        throw FileNotFoundException("Could not find anything here: $extensionPath")
    }
    if (file.isFile) {
        val tempDir = createTempDirectory(prefix = "extension_").toFile()
        ZipFile(file).use { zip ->
            zip.entries().asSequence().forEach { entry ->
                val file = File(tempDir, entry.name)
                if (entry.isDirectory) {
                    file.mkdirs()
                } else {
                    file.outputStream().use { output ->
                        zip.getInputStream(entry).copyTo(output)
                    }
                }
            }
        }
        _extensions.add(Path(tempDir.absolutePath))
    } else if (file.isDirectory) {
        val manifestFile = file.walkTopDown().find { it.name.startsWith("manifest.") }
        if (manifestFile != null) {
            _extensions.add(Path(manifestFile.parentFile.absolutePath))
        } else {
            throw FileNotFoundException("Manifest file not found in directory: $extensionPath")
        }
    }
}
