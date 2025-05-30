package dev.kdriver.core.browser

import org.slf4j.LoggerFactory
import java.io.File
import java.io.FileNotFoundException
import java.nio.file.Path
import java.util.zip.ZipFile
import kotlin.io.path.createTempDirectory

class Config(
    userDataDir: Path? = null,
    val headless: Boolean = false,
    browserExecutablePath: Path? = null,
    browserArgs: List<String>? = null,
    sandbox: Boolean = true,
    val lang: String = "en-US",
    var host: String? = null,
    var port: Int? = null,
    val expert: Boolean = false,
    val browserConnectionTimeout: Long = 500,
    val browserConnectionMaxTries: Int = 60,
    val eventsBufferSize: Int = 64,
    val autoDiscoverTargets: Boolean = true,
) {

    private val logger = LoggerFactory.getLogger("Config")

    private var _userDataDir: Path? = null
    private var _customDataDir: Boolean = false

    private val _browserArgs: MutableList<String> = browserArgs?.toMutableList() ?: mutableListOf()
    private val _extensions: MutableList<Path> = mutableListOf()

    val browserExecutablePath: Path = browserExecutablePath ?: findChromeExecutable()

    var sandbox: Boolean = sandbox
        private set

    init {
        if (isPosix() && isRoot() && sandbox) {
            logger.info("Detected root usage, auto disabling sandbox mode")
            this.sandbox = false
        }
        userDataDir?.let {
            this.userDataDir = it
        }
    }

    var userDataDir: Path
        get() {
            if (_userDataDir == null) {
                _userDataDir = tempProfileDir()
                _customDataDir = false
            }
            return _userDataDir!!
        }
        set(value) {
            _userDataDir = value
            _customDataDir = true
        }

    val usesCustomDataDir: Boolean
        get() = _customDataDir

    private val defaultBrowserArgs: List<String> = listOf(
        "--remote-allow-origins=*",
        "--no-first-run",
        "--no-service-autorun",
        "--no-default-browser-check",
        "--homepage=about:blank",
        "--no-pings",
        "--password-store=basic",
        "--disable-infobars",
        "--disable-breakpad",
        "--disable-component-update",
        "--disable-backgrounding-occluded-windows",
        "--disable-renderer-backgrounding",
        "--disable-background-networking",
        "--disable-dev-shm-usage",
        "--disable-features=IsolateOrigins,DisableLoadExtensionCommandLineSwitch,site-per-process",
        "--disable-session-crashed-bubble",
        "--disable-search-engine-choice-screen"
    )

    val browserArgs: List<String>
        get() = (defaultBrowserArgs + _browserArgs).distinct().sorted()

    val extensions: List<Path>
        get() = _extensions.toList()

    fun addExtension(extensionPath: Path) {
        val path = extensionPath.toFile()
        if (!path.exists()) {
            throw FileNotFoundException("Could not find anything here: $extensionPath")
        }
        if (path.isFile) {
            val tempDir = createTempDirectory(prefix = "extension_").toFile()
            ZipFile(path).use { zip ->
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
            _extensions.add(tempDir.toPath())
        } else if (path.isDirectory) {
            val manifestFile = path.walkTopDown().find { it.name.startsWith("manifest.") }
            if (manifestFile != null) {
                _extensions.add(manifestFile.parentFile.toPath())
            } else {
                throw FileNotFoundException("Manifest file not found in directory: $extensionPath")
            }
        }
    }

    operator fun invoke(): List<String> {
        val args = mutableListOf<String>()
        args.addAll(defaultBrowserArgs)
        args.add("--user-data-dir=$userDataDir")
        if (expert) {
            args.addAll(listOf("--disable-web-security", "--disable-site-isolation-trials"))
        }
        args.addAll(_browserArgs.filter { it !in args })
        if (headless) {
            args.add("--headless=new")
        }
        if (!sandbox) {
            args.add("--no-sandbox")
        }
        host?.let { args.add("--remote-debugging-host=$it") }
        port?.let { args.add("--remote-debugging-port=$it") }
        return args
    }

    fun addArgument(arg: String) {
        val forbiddenArgs = listOf("headless", "data-dir", "data_dir", "no-sandbox", "no_sandbox", "lang")
        if (forbiddenArgs.any { arg.contains(it, ignoreCase = true) }) {
            throw IllegalArgumentException("\"$arg\" not allowed. Please use one of the attributes of the Config object to set it")
        }
        _browserArgs.add(arg)
    }

    companion object {
        fun isPosix(): Boolean {
            val os = System.getProperty("os.name").lowercase()
            return os.contains("nix") || os.contains("nux") || os.contains("mac")
        }

        fun isRoot(): Boolean {
            return try {
                val process = ProcessBuilder("id", "-u").start()
                val result = process.inputStream.bufferedReader().readText().trim()
                result == "0"
            } catch (e: Exception) {
                false
            }
        }

        private fun tempProfileDir(): Path {
            return createTempDirectory(prefix = "uc_")
        }

        private fun findChromeExecutable(): Path {
            val candidates = mutableListOf<Path>()
            val os = System.getProperty("os.name").lowercase()
            val paths = System.getenv("PATH")?.split(File.pathSeparator) ?: emptyList()
            if (isPosix()) {
                val executables = listOf(
                    "google-chrome",
                    "chromium",
                    "chromium-browser",
                    "chrome",
                    "google-chrome-stable"
                )
                for (path in paths) {
                    for (exe in executables) {
                        val candidate = File(path, exe)
                        if (candidate.exists() && candidate.canExecute()) {
                            candidates.add(candidate.toPath())
                        }
                    }
                }
                if (os.contains("mac")) {
                    candidates.addAll(
                        listOf(
                            Path.of("/Applications/Google Chrome.app/Contents/MacOS/Google Chrome"),
                            Path.of("/Applications/Chromium.app/Contents/MacOS/Chromium")
                        )
                    )
                }
            } else {
                val programFiles = listOfNotNull(
                    System.getenv("PROGRAMFILES"),
                    System.getenv("PROGRAMFILES(X86)"),
                    System.getenv("LOCALAPPDATA"),
                    System.getenv("PROGRAMW6432")
                )
                val subPaths = listOf(
                    "Google/Chrome/Application",
                    "Google/Chrome Beta/Application",
                    "Google/Chrome Canary/Application",
                    "Google/Chrome SxS/Application",
                )
                for (base in programFiles) {
                    for (sub in subPaths) {
                        val candidate = File(base, "$sub/chrome.exe")
                        candidates.add(candidate.toPath())
                    }
                }
            }
            return candidates
                .filter {
                    val file = it.toFile()
                    file.exists() && file.canExecute()
                }
                .minByOrNull { it.toAbsolutePath().toString().length }
                ?: throw FileNotFoundException("Could not find a valid Chrome browser binary. Please make sure Chrome is installed or specify the 'browserExecutablePath' parameter.")
        }
    }

}
