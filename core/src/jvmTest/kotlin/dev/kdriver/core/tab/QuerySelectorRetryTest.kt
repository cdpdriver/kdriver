package dev.kdriver.core.tab

import dev.kdriver.cdp.CDPException
import dev.kdriver.cdp.CommandMode
import dev.kdriver.core.dom.NodeOrElement
import dev.kdriver.cdp.domain.DOM
import dev.kdriver.cdp.domain.Target
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.JsonElement
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Guards the retry behavior of [DefaultTab.querySelector] / [querySelectorAll] (audit ISSUE-7).
 *
 * When CDP reports "could not find node", the code retries **once** (after refreshing the node)
 * and then gives up. The retry-once guard (`lastMap`) was declared as a method-local that is
 * recreated on every recursive call, so it never tripped and the method recursed without bound
 * (StackOverflowError / hang) whenever the error persisted.
 *
 * These tests drive the real production methods through a stubbed [callCommand] (no browser),
 * with a hard cap that turns runaway recursion into a clear failure instead of a noisy crash.
 */
class QuerySelectorRetryTest {

    private class StubTab(
        scope: CoroutineScope,
        private val onSelectorCall: () -> Unit,
    ) : DefaultTab(
        websocketUrl = "ws://stub/devtools/page/stub",
        messageListeningScope = scope,
        targetInfo = TARGET,
    ) {
        override suspend fun callCommand(
            method: String,
            parameter: JsonElement?,
            mode: CommandMode,
        ): JsonElement? = when (method) {
            "DOM.querySelector", "DOM.querySelectorAll" -> {
                onSelectorCall()
                throw CDPException(
                    method = method,
                    code = -32000,
                    originalMessage = "Could not find node with given id",
                    data = null,
                )
            }
            // DOM.disable and anything else: succeed with no result.
            else -> null
        }
    }

    private fun node(nodeId: Int) = DOM.Node(
        nodeId = nodeId,
        backendNodeId = nodeId,
        nodeType = 1,
        nodeName = "DIV",
        localName = "div",
        nodeValue = "",
    )

    @Test
    fun querySelector_retriesOnceThenGivesUp_onPersistentNotFound() = runTest {
        var calls = 0
        val tab = StubTab(this) {
            calls++
            // Safety net: without the fix the recursion never terminates. Fail loudly and
            // deterministically instead of letting it grow into a StackOverflowError.
            check(calls <= CAP) { "querySelector recursed $calls times — retry guard not working (ISSUE-7)" }
        }

        val result = tab.querySelector("div.missing", NodeOrElement.WrappedNode(node(1)))

        assertNull(result, "Persistent 'could not find node' should resolve to null")
        assertEquals(2, calls, "Should attempt exactly once + one retry")
    }

    @Test
    fun querySelectorAll_retriesOnceThenGivesUp_onPersistentNotFound() = runTest {
        var calls = 0
        val tab = StubTab(this) {
            calls++
            check(calls <= CAP) { "querySelectorAll recursed $calls times — retry guard not working (ISSUE-7)" }
        }

        val result = tab.querySelectorAll("div.missing", NodeOrElement.WrappedNode(node(1)))

        assertTrue(result.isEmpty(), "Persistent 'could not find node' should resolve to empty list")
        assertEquals(2, calls, "Should attempt exactly once + one retry")
    }

    private companion object {
        const val CAP = 8
        val TARGET = Target.TargetInfo(
            targetId = "stub",
            type = "page",
            title = "",
            url = "about:blank",
            attached = true,
            canAccessOpener = false,
        )
    }
}
