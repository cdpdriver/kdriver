package dev.kdriver.core.browser

/**
 * Reports the exit status of a process that has already terminated, or null if it is still running
 * (or the platform cannot tell).
 *
 * Paired with [Process.isAlive]: `isAlive() == false` says the browser is gone, this says *how* it
 * went. The distinction is the whole point — on Windows a Chrome that finds a live instance on the
 * same `--user-data-dir` hands its command line over and exits **0**, silently, without ever opening
 * a debug port; a Chrome that crashed exits non-zero. Both look identical without this.
 */
expect fun Process.exitCodeOrNull(): Int?

/**
 * Describes what became of the browser process.
 *
 * Reports the exit status as-is rather than interpreting it: the whole reason for reading it is that
 * we do not yet know which statuses a browser that never opened its debug port exits with. Guessing
 * here would put an unverified claim into every log line.
 *
 * @param pid the browser's process id, if we had one.
 * @param alive whether the process was still running when we gave up.
 * @param exitCode its exit status, or null if it is alive or the platform cannot tell.
 *
 * @return a fragment such as `pid=1916, exited with 21`, meant to be embedded in a larger message.
 */
internal fun processFate(pid: Long?, alive: Boolean, exitCode: Int?): String = "pid=$pid, " + when {
    alive -> "still running"
    exitCode == null -> "already gone (exit status unavailable)"
    else -> "exited with $exitCode"
}

/**
 * Builds the message logged when a browser never opened its debug port.
 *
 * Pure and separated from [DefaultBrowser.start] so the wording is covered by tests on any OS — the
 * facts it reports come from platform calls that cannot be exercised in CI.
 *
 * @param endpoint the address the debug port was expected on.
 * @param waitedMs how long we waited for it.
 * @param fate the process's id and what became of it — see [processFate].
 * @param lastConnectionError the last failure seen while polling, if any.
 * @param stderr whatever the browser wrote to stderr, if anything.
 *
 * @return a single line naming the endpoint, the process's fate, and the last connection error.
 */
internal fun browserStartFailureMessage(
    endpoint: String,
    waitedMs: Long,
    fate: String,
    lastConnectionError: Throwable?,
    stderr: String?,
): String = "Browser never opened its debug port on $endpoint after ${waitedMs}ms ($fate). " +
    "Last connection error: " +
    (lastConnectionError?.let { "${it::class.simpleName}: ${it.message}" } ?: "none") +
    ". Browser stderr: " + (stderr?.trim()?.takeIf { it.isNotEmpty() } ?: "<none>")
