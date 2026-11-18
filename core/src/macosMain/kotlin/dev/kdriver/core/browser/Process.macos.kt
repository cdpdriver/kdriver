package dev.kdriver.core.browser

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import kotlinx.io.files.Path
import platform.Foundation.NSPipe
import platform.Foundation.NSTask
import platform.Foundation.launch
import platform.Foundation.launchPath

private class NSTaskProcess(private val task: NSTask) : Process() {
    override val processIdentifier: Int
        get() = task.processIdentifier

    /** NSTask reaps its own child, and knows when it has exited. */
    override fun hasExited(): Boolean = !task.isRunning()

    override fun destroy() {
        if (task.isRunning()) {
            task.terminate()
        }
    }
}

actual suspend fun startProcess(
    exe: Path,
    params: List<String>,
): Process = withContext(Dispatchers.IO) {
    val task = NSTask()
    task.launchPath = exe.toString()
    task.arguments = params

    // Set up pipes for stdin, stdout, stderr
    task.standardInput = NSPipe()
    task.standardOutput = NSPipe()
    task.standardError = NSPipe()

    task.launch()
    NSTaskProcess(task)
}

actual fun defaultBrowserSearchConfig(): BrowserSearchConfig {
    return BrowserSearchConfig(":", searchMacosApplications = true)
}
