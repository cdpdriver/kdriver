package dev.kdriver.cdp

enum class CommandMode {

    /**
     * Default command mode.
     */
    DEFAULT,

    /**
     * One-shot command mode.
     * Equivalent to Zendriver's `_send_oneshot` method.
     */
    ONE_SHOT,

}
