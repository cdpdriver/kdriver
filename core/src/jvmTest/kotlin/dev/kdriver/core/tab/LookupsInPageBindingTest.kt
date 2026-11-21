@file:OptIn(dev.kdriver.cdp.InternalCdpApi::class)

package dev.kdriver.core.tab

import dev.kdriver.cdp.CDPException
import dev.kdriver.core.ScriptedTab
import dev.kdriver.core.ScriptedTransport
import dev.kdriver.core.ScriptedTransport.Reply
import dev.kdriver.core.dom.DefaultElement
import dev.kdriver.core.dom.Element
import dev.kdriver.core.dom.NodeOrElement
import dev.kdriver.core.exceptions.EvaluateException
import dev.kdriver.core.exceptions.TimeoutWaitingForElementException
import dev.kdriver.core.objectIdsOf
import dev.kdriver.core.param
import dev.kdriver.core.valueReply
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * The elements `select`, `selectAll`, `querySelector` and `querySelectorAll` find in the page get their node ids, tree
 * and parent from the document when they are updated, read after the document was read, or when a lookup runs under
 * them; a frame is searched with the page from one document read.
 */
class LookupsInPageBindingTest: LookupsInPageFixtures() {


    private val documentWithAFrame =
        """{"root":{"nodeId":1,"backendNodeId":1,"nodeType":9,"nodeName":"#document","localName":"","nodeValue":"",""" +
            """"childNodeCount":2,"children":[{"nodeId":7,"backendNodeId":70,"nodeType":1,"nodeName":"P",""" +
            """"localName":"p","nodeValue":"","childNodeCount":1,"children":[""" +
            """{"nodeId":8,"backendNodeId":80,"nodeType":3,"nodeName":"#text","localName":"",""" +
            """"nodeValue":"Outer"}]},""" +
            """{"nodeId":9,"backendNodeId":90,"nodeType":1,"nodeName":"IFRAME","localName":"iframe","nodeValue":"",""" +
            """"childNodeCount":0,"contentDocument":{"nodeId":10,"backendNodeId":100,"nodeType":9,""" +
            """"nodeName":"#document","localName":"","nodeValue":"","childNodeCount":1,"children":[""" +
            """{"nodeId":11,"backendNodeId":110,"nodeType":1,"nodeName":"P","localName":"p","nodeValue":"",""" +
            """"childNodeCount":1,"children":[{"nodeId":12,"backendNodeId":120,"nodeType":3,"nodeName":"#text",""" +
            """"localName":"","nodeValue":"Inner"}]}]}}]}}"""

    @Test
    fun selectAllWithAFrame_findsInsideItAndInThePage_fromOneDocumentRead() = runTest(StandardTestDispatcher()) {
        val tab = tab {
            when (it.method) {
                "DOM.getDocument" -> Reply.ok(documentWithAFrame)
                "DOM.querySelectorAll" -> when (it.param("nodeId")) {
                    "1" ->
                        if (it.param("selector") == "iframe") Reply.ok("""{"nodeIds":[9]}""")
                        else Reply.ok("""{"nodeIds":[7]}""")
                    "10" -> Reply.ok("""{"nodeIds":[11]}""")
                    else -> Reply.error("Could not find node with given id")
                }
                "DOM.resolveNode" -> Reply.ok("""{"object":{"type":"object","subtype":"node","objectId":"frame"}}""")
                else -> Reply.ok()
            }
        }

        val found = tab.selectAll("p", timeout = 1_000, includeFrames = true)

        assertEquals(listOf("Inner", "Outer"), found.map { it.text })
        assertEquals(1, tab.sent.count { it == "DOM.getDocument" }, "one read for the frame and the page")
        assertTrue("Runtime.evaluate" !in tab.sent)
        tab.close()
    }

    @Test
    fun selectAllWithFrames_readsTheDocument() = runTest(StandardTestDispatcher()) {
        val tab = tab {
            when (it.method) {
                "DOM.getDocument" -> Reply.ok(document)
                "DOM.querySelectorAll" ->
                    if (it.param("selector") == "iframe") Reply.ok("""{"nodeIds":[]}""")
                    else Reply.ok("""{"nodeIds":[7]}""")
                else -> Reply.ok()
            }
        }

        assertEquals(listOf("Send"), tab.selectAll("#go", timeout = 1_000, includeFrames = true).map { it.text })
        assertEquals(
            listOf("iframe", "#go"),
            tab.transport.sent.filter { it.method == "DOM.querySelectorAll" }.map { it.param("selector") }
        )
        assertEquals(1, tab.sent.count { it == "DOM.getDocument" }, "one read for the frames and the page")
        assertTrue("Runtime.evaluate" !in tab.sent)
        tab.close()
    }

    @Test
    fun aLookupUnderAnElementFoundInThePage_bindsItToTheDocumentFirst() = runTest(StandardTestDispatcher()) {
        val tab = tab {
            when (it.method) {
                "Runtime.evaluate" -> Reply.ok(found("obj-1"))
                "DOM.describeNode" -> Reply.ok(button)
                "DOM.getDocument" -> Reply.ok(document)
                "DOM.querySelector" -> Reply.ok("""{"nodeId":8}""")
                else -> Reply.ok()
            }
        }
        val button = tab.querySelector("#go")!!

        val text = tab.querySelector("*", NodeOrElement.WrappedElement(button))

        assertEquals(7, button.node.nodeId, "bound to the document")
        assertEquals(1, button.tree?.nodeId)
        assertEquals("7", tab.transport.sent.single { it.method == "DOM.querySelector" }.param("nodeId"))
        assertEquals(8, text!!.node.nodeId)
        assertNull(button.objectId, "bound without resolving its object")
        tab.querySelector("*", NodeOrElement.WrappedElement(button))
        assertEquals(1, tab.sent.count { it == "DOM.getDocument" }, "a bound element is not bound again")
        assertEquals(2, tab.sent.count { it == "DOM.querySelector" })
        assertTrue("DOM.resolveNode" !in tab.sent)
        tab.close()
    }

    /** `<ul id="list"><li class="item">Beta</li></ul>`, as the document read gives it. */
    private val list =
        """{"root":{"nodeId":1,"backendNodeId":1,"nodeType":9,"nodeName":"#document","localName":"","nodeValue":"",""" +
            """"childNodeCount":1,"children":[{"nodeId":5,"parentId":1,"backendNodeId":50,"nodeType":1,""" +
            """"nodeName":"UL","localName":"ul","nodeValue":"","attributes":["id","list"],"childNodeCount":1,""" +
            """"children":[{"nodeId":7,"parentId":5,"backendNodeId":70,"nodeType":1,"nodeName":"LI",""" +
            """"localName":"li","nodeValue":"","attributes":["class","item"],"childNodeCount":1,"children":[""" +
            """{"nodeId":8,"parentId":7,"backendNodeId":80,"nodeType":3,"nodeName":"#text","localName":"",""" +
            """"nodeValue":"Beta"}]}]}]}}"""

    /** The same `li`, as `DOM.describeNode` gives it: no node ids, no parent id. */
    private val describedItem =
        """{"node":{"nodeId":0,"backendNodeId":70,"nodeType":1,"nodeName":"LI","localName":"li","nodeValue":"",""" +
            """"attributes":["class","item"],"childNodeCount":1,"children":[{"nodeId":0,"parentId":0,""" +
            """"backendNodeId":80,"nodeType":3,"nodeName":"#text","localName":"","nodeValue":"Beta"}]}}"""

    private fun TestScope.listTab() = tab {
        when (it.method) {
            "Runtime.evaluate" -> Reply.ok(found("obj-1"))
            "DOM.describeNode" -> Reply.ok(describedItem)
            "DOM.getDocument" -> Reply.ok(list)
            "DOM.querySelector" -> Reply.ok("""{"nodeId":7}""")
            "DOM.resolveNode" -> Reply.ok("""{"object":{"type":"object","subtype":"node","objectId":"obj-2"}}""")
            "Target.getTargetInfo" -> Reply.ok(ScriptedTab.TARGET_INFO)
            else -> Reply.ok()
        }
    }

    // What a lookup on the whole document gave: DOM.querySelector on the document read, the match taken from it.
    private suspend fun ScriptedTab.onTheDocument(selector: String): Element =
        querySelector(selector, NodeOrElement.WrappedNode(currentDocument().first))!!

    @Test
    fun anElementFoundInThePage_hasNoNodeIdTreeNorParent_untilUpdated() = runTest(StandardTestDispatcher()) {
        val tab = listTab()

        val item = tab.select("li.item", timeout = 1_000)

        assertEquals(0, item.node.nodeId)
        assertNull(item.parentId)
        assertNull(item.tree)
        assertEquals(
            "could not get parent since the element has no tree set",
            assertFailsWith<RuntimeException> { item.parent }.message,
        )
        assertEquals(listOf(0), item.children.map { it.node.nodeId })
        assertTrue("DOM.getDocument" !in tab.sent)
        tab.close()
    }

    @Test
    fun update_givesTheNodeIdsTreeAndParent_aLookupOnTheDocumentGave() = runTest(StandardTestDispatcher()) {
        val tab = listTab()
        val item = tab.select("li.item", timeout = 1_000)

        item.update()
        val before = tab.onTheDocument("li.item")

        assertEquals(before.node, item.node)
        assertEquals(7, item.node.nodeId)
        assertEquals(5, item.parentId)
        assertEquals(before.tree, item.tree)
        assertEquals(before.parent!!.node, item.parent!!.node)
        assertEquals("ul", item.parent!!.tag)
        assertEquals(before.children.map { it.node }, item.children.map { it.node })
        assertEquals(1, tab.sent.count { it == "DOM.getDocument" }, "one document read for both")
        tab.close()
    }

    @Test
    fun anElementFoundInThePage_fillsItsNodeIdsTreeAndParent_onFirstRead_fromAKeptDocument() =
        runTest(StandardTestDispatcher()) {
            val tab = listTab()
            val before = tab.onTheDocument("li.item")
            val item = tab.select("li.item", timeout = 1_000)
            val sentBefore = tab.sent.size

            assertEquals(before.node, item.node)
            assertEquals(7, item.node.nodeId)
            assertEquals(5, item.parentId)
            assertEquals(before.tree, item.tree)
            assertEquals("ul", item.parent!!.tag)
            assertEquals(listOf(8), item.children.map { it.node.nodeId })
            assertEquals(sentBefore, tab.sent.size, "filled from the kept document, with no command")
            assertEquals(1, tab.sent.count { it == "DOM.getDocument" })
            tab.close()
        }

    @Test
    fun anElementFoundInThePage_isNotFilledFromADocumentThePageChangedSince() = runTest(StandardTestDispatcher()) {
        val tab = listTab()
        tab.onTheDocument("li.item")
        tab.transport.event("DOM.attributeModified", """{"nodeId":7,"name":"class","value":"busy"}""")
        runCurrent()

        val item = tab.select("li.item", timeout = 1_000)

        assertEquals(0, item.node.nodeId)
        assertNull(item.tree)
        item.update()
        assertEquals(7, item.node.nodeId, "update() still reads the document")
        assertEquals(2, tab.sent.count { it == "DOM.getDocument" })
        tab.close()
    }

    @Test
    fun anElementFoundInThePage_whoseNodeTheKeptDocumentLacks_staysAsFound() = runTest(StandardTestDispatcher()) {
        val tab = tab {
            when (it.method) {
                "Runtime.evaluate" -> Reply.ok(found("obj-1"))
                "DOM.describeNode" -> Reply.ok(item(30, "Gone"))
                "DOM.getDocument" -> Reply.ok(list)
                "Target.getTargetInfo" -> Reply.ok(ScriptedTab.TARGET_INFO)
                else -> Reply.ok()
            }
        }
        tab.currentDocument()
        val gone = tab.select("li", timeout = 1_000)

        assertEquals(0, gone.node.nodeId)
        assertNull(gone.tree)
        assertEquals("Gone", gone.text)
        (gone as DefaultElement).node = gone.node.copy(nodeName = "P")
        assertEquals("p", gone.tag, "a node set by hand is kept")
        tab.close()
    }

    @Test
    fun aBareNodeFoundInThePage_isReplacedByTheDocumentNode_forALookupUnderIt() = runTest(StandardTestDispatcher()) {
        val tab = listTab()
        val item = tab.querySelector("li.item")!!

        tab.querySelector("*", NodeOrElement.WrappedNode(item.node))

        assertEquals("7", tab.transport.sent.single { it.method == "DOM.querySelector" }.param("nodeId"))
        tab.close()
    }

    @Test
    fun aNodeFoundInThePage_butGoneFromTheDocument_isQueriedAsItIs() = runTest(StandardTestDispatcher()) {
        val tab = tab {
            when (it.method) {
                "Runtime.evaluate" -> Reply.ok(found("obj-1"))
                "DOM.describeNode" -> Reply.ok(item(30, "Gone"))
                "DOM.getDocument" -> Reply.ok(list)
                "DOM.querySelector" -> Reply.error("Could not find node with given id")
                "DOM.resolveNode" -> Reply.ok("""{"object":{"type":"object","subtype":"node","objectId":"obj-2"}}""")
                else -> Reply.ok()
            }
        }
        val gone = tab.querySelector("li")!!

        assertNull(tab.querySelector("*", NodeOrElement.WrappedNode(gone.node)))
        assertNull(tab.querySelector("*", NodeOrElement.WrappedElement(gone)))

        assertEquals(0, gone.node.nodeId, "no node of the document to take")
        assertEquals(1, gone.tree?.nodeId, "the element is bound to the document all the same, as update() does")
        val queried = tab.transport.sent.filter { it.method == "DOM.querySelector" }.map { it.param("nodeId") }
        assertEquals(listOf("0", "0"), queried.take(2))
        tab.close()
    }

    @Test
    fun anotherElementImplementation_isUpdated_beforeALookupUnderIt() = runTest(StandardTestDispatcher()) {
        val tab = listTab()
        val found = tab.querySelector("li.item")!!
        val wrapper: Element = object : Element by found {}

        tab.querySelector("*", NodeOrElement.WrappedElement(wrapper))

        assertEquals(7, wrapper.node.nodeId)
        assertTrue("DOM.resolveNode" in tab.sent, "Element.update resolves the object, as it always did")
        tab.close()
    }

    @Test
    fun querySelectorAll_readsTheDocument_whenTheMatchesOutweighIt() = runTest(StandardTestDispatcher()) {
        val tab = tab {
            when (it.method) {
                "Runtime.evaluate" -> Reply.ok("""{"result":{"type":"string","value":"document"}}""")
                "DOM.getDocument" -> Reply.ok(list)
                "DOM.querySelectorAll" -> Reply.ok("""{"nodeIds":[5,7]}""")
                else -> Reply.ok()
            }
        }

        val found = tab.querySelectorAll("ul, li")

        assertEquals(listOf(5, 7), found.map { it.node.nodeId }, "read from the document, as before")
        assertEquals(listOf(1, 1), found.map { it.tree?.nodeId })
        assertEquals(listOf("Runtime.evaluate", "DOM.getDocument", "DOM.querySelectorAll"), tab.sent)
        tab.close()
    }

    @Test
    fun anElementOfQuerySelectorAll_resolvesItsNodeAtItsFirstCall() = runTest(StandardTestDispatcher()) {
        val tab = tab {
            when (it.method) {
                "Runtime.evaluate" -> Reply.ok(found("array", subtype = "array"))
                "Runtime.getProperties" -> Reply.ok(arrayOfOne)
                "DOM.describeNode" -> Reply.ok(item(10, "Alpha"))
                "DOM.resolveNode" -> Reply.ok("""{"object":{"type":"object","subtype":"node","objectId":"obj-2"}}""")
                "Runtime.callFunctionOn" -> Reply.ok(valueReply("null"))
                else -> Reply.ok()
            }
        }

        tab.querySelectorAll(".item").single().click()

        assertEquals("10", tab.transport.sent.single { it.method == "DOM.resolveNode" }.param("backendNodeId"))
        assertEquals(listOf("obj-2"), tab.transport.objectIdsOf())
        tab.close()
    }
}
