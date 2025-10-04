package dev.kdriver.core.utils

import kotlinx.io.files.Path


actual abstract class Process {
    actual fun isAlive(): Boolean {
        throw UnsupportedOperationException()
    }

    actual fun pid(): Long {
        throw UnsupportedOperationException()
    }

    actual abstract fun destroy()
}

actual suspend fun startProcess(
    exe: Path,
    params: List<String>,
): Process {
    throw UnsupportedOperationException()
}

actual fun addShutdownHook(hook: suspend () -> Unit) {
    // Do nothing for now
}

actual fun isPosix(): Boolean {
    throw UnsupportedOperationException()
}

actual fun isRoot(): Boolean {
    throw UnsupportedOperationException()
}

actual fun tempProfileDir(): Path {
    throw UnsupportedOperationException()
}

actual fun exists(path: Path): Boolean {
    throw UnsupportedOperationException()
}

actual fun findChromeExecutable(): Path? {
    throw UnsupportedOperationException()
}

actual fun findOperaExecutable(): Path? {
    throw UnsupportedOperationException()
}

actual fun findBraveExecutable(): Path? {
    throw UnsupportedOperationException()
}

actual fun findEdgeExecutable(): Path? {
    throw UnsupportedOperationException()
}

actual fun freePort(): Int? {
    throw UnsupportedOperationException()
}

actual fun decompressIfNeeded(data: ByteArray): ByteArray {
    throw UnsupportedOperationException()
}
