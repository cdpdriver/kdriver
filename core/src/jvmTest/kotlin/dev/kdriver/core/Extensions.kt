package dev.kdriver.core

import kotlinx.io.files.Path
import java.io.File
import java.net.URI

fun sampleFile(name: String): String {
    val resource = checkNotNull(Thread.currentThread().contextClassLoader.getResource(name)) {
        "Resource '$name' not found"
    }
    return resource.toURI().toString()
}

fun samplePath(name: String): Path {
    return Path(File(URI(sampleFile(name))).absolutePath)
}
