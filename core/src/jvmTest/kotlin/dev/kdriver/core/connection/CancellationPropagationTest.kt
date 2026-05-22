package dev.kdriver.core.connection

import dev.kdriver.cdp.CommandMode
import dev.kdriver.cdp.Serialization
import dev.kdriver.cdp.domain.DOM
import dev.kdriver.core.tab.DefaultTab
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.encodeToJsonElement
import kotlin.coroutines.cancellation.CancellationException
import kotlin.test.Test
import kotlin.test.assertFailsWith

/**
 * Guards against [CancellationException] being swallowed by broad `catch (e: Exception)` /
 * `runCatching {}` blocks in suspend code (audit ISSUE-19).
 *
 * In Kotlin, `CancellationException` is an `Exception`, so a `catch (e: Exception)` (or
 * `runCatching`, which catches `Throwable`) silently absorbs it and breaks cooperative
 * cancellation. These tests drive the *real* production code through a stubbed [callCommand],
 * so no browser is required.
 */
class CancellationPropagationTest {

    /**
     * A [DefaultTab] whose CDP transport is replaced by a canned map of method -> response.
     * A method may instead throw, simulating the coroutine being cancelled mid-call.
     */
    private class StubTab(
        scope: CoroutineScope,
        private val responder: (method: String) -> JsonElement?,
    ) : DefaultTab(
        websocketUrl = "ws://stub/devtools/page/stub",
        messageListeningScope = scope,
        targetInfo = DOM_TARGET,
    ) {
        override suspend fun callCommand(
            method: String,
            parameter: JsonElement?,
            mode: CommandMode,
        ): JsonElement? = responder(method)
    }

    private fun node(nodeId: Int) = DOM.Node(
        nodeId = nodeId,
        backendNodeId = nodeId,
        nodeType = 1,
        nodeName = "HTML",
        localName = "html",
        nodeValue = "",
    )

    /**
     * `findElementsByText` resolves search hits that aren't in the local tree via
     * `dom.resolveNode(...)`, wrapped in `catch (_: Exception) { null }` (DefaultTab.kt ~:492).
     * If that CDP call is cancelled, the `CancellationException` must propagate, not be turned
     * into "node skipped".
     *
     * RED (unfixed): the cancellation is swallowed and the function returns normally
     * (an empty list), so `assertFailsWith` fails because nothing is thrown.
     */
    @Test
    fun findElementsByText_propagatesCancellation_fromResolveNode() = runTest {
        val tab = StubTab(this) { method ->
            when (method) {
                "DOM.getDocument" ->
                    Serialization.json.encodeToJsonElement(DOM.GetDocumentReturn(root = node(1)))
                "DOM.performSearch" ->
                    Serialization.json.encodeToJsonElement(
                        DOM.PerformSearchReturn(searchId = "s1", resultCount = 1)
                    )
                "DOM.getSearchResults" ->
                    // A nodeId that is NOT present in the (childless) document root, so the
                    // code falls into the resolveNode branch.
                    Serialization.json.encodeToJsonElement(
                        DOM.GetSearchResultsReturn(nodeIds = listOf(999))
                    )
                "DOM.discardSearchResults" -> null
                "DOM.resolveNode" -> throw CancellationException("cancelled during resolveNode")
                else -> null
            }
        }

        assertFailsWith<CancellationException> {
            tab.findElementsByText("anything")
        }
    }

    private companion object {
        val DOM_TARGET = dev.kdriver.cdp.domain.Target.TargetInfo(
            targetId = "stub",
            type = "page",
            title = "",
            url = "about:blank",
            attached = true,
            canAccessOpener = false,
        )
    }
}
