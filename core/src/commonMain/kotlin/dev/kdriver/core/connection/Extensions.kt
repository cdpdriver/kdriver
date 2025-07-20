package dev.kdriver.core.connection

import dev.kdriver.cdp.CDP
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

/**
 * Sends a CDP command and waits for the response.
 *
 * This is an alias so that you can use cdp the same way as zendriver does:
 * ```kotlin
 * // send a network.enable command with kdriver
 * tab.send { cdp.network.enable() }
 * ```
 * That would be equivalent to this with zendriver:
 * ```python
 * # send a network.enable command with zendriver
 * tab.send(cdp.network.enable())
 * ```
 *
 * Although you can directly call the CDP methods on the tab (recommended way of doing it):
 * ```kotlin
 * // send a network.enable command with kdriver, directly
 * tab.network.enable()
 * ```
 *
 * @param command The command to send. This is a suspending function that can call any CDP method.
 *
 * @return The result of the command, deserialized to type T.
 */
inline fun <T> Connection.send(command: CDP.() -> T): T {
    return this.command()
}


/**
 * Adds a handler for a specific CDP event.
 *
 * This is an alias so that you can use cdp the same way as zendriver does:
 * ```kotlin
 * // add a handler for the consoleAPICalled event with kdriver
 * tab.addHandler(this, { cdp.runtime.consoleAPICalled }) { event ->
 *     println(event)
 * }
 * ```
 * That would be equivalent to this with zendriver:
 * ```python
 * # add a handler for the consoleAPICalled event with zendriver
 * tab.add_handler(cdp.runtime.ConsoleAPICalled, lambda event: print(event))
 * ```
 *
 * Although you can directly collect the events from the tab (recommended way of doing it):
 * ```kotlin
 * // add a handler for the consoleAPICalled event with kdriver, directly
 * launch {
 *     tab.runtime.consoleAPICalled.collect { event ->
 *         println(event)
 *     }
 * }
 * ```
 *
 * @param coroutineScope The coroutine scope in which the handler will run.
 * @param event A lambda that returns a Flow of the event type to listen to.
 * @param handler A suspend function that will be called with each event of the specified type.
 *
 * @return A Job that can be used to cancel the handler.
 */
inline fun <T> Connection.addHandler(
    coroutineScope: CoroutineScope,
    crossinline event: CDP.() -> Flow<T>,
    crossinline handler: suspend (T) -> Unit,
): Job {
    return coroutineScope.launch {
        event().collect { handler(it) }
    }
}
