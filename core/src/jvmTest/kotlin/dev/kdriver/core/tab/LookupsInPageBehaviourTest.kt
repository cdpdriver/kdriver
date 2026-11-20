package dev.kdriver.core.tab

import dev.kdriver.cdp.CDPException
import dev.kdriver.cdp.domain.DOM
import dev.kdriver.cdp.domain.dom
import dev.kdriver.core.browser.Browser
import dev.kdriver.core.browser.createBrowser
import dev.kdriver.core.dom.Element
import dev.kdriver.core.dom.NodeOrElement
import dev.kdriver.core.exceptions.TimeoutWaitingForElementException
import dev.kdriver.core.sampleFile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * On a real page, the lookups the page runs (`select`, `selectAll`, `querySelector`, `querySelectorAll` without a
 * node) find what a lookup on the whole document finds (`DOM.getDocument`, then `DOM.querySelector(All)` on it, as
 * these methods did before): the same elements, text, attributes, children and order, the same absence inside shadow
 * roots and frames, elements that can be clicked; once updated, the same node ids, tree and parent; and for an invalid
 * selector, the same exception.
 */
class LookupsInPageBehaviourTest {

    private fun onPage(block: suspend Tab.() -> Unit) = runBlocking {
        val browser: Browser = createBrowser(this as CoroutineScope, headless = true, sandbox = false)
        try {
            browser.get(sampleFile("lookup-test.html")).block()
        } finally {
            browser.stop()
        }
    }

    // A lookup on the whole document, as these methods did before: the document read, then queried by node id.
    private suspend fun Tab.allOnTheDocument(selector: String): List<Element> =
        querySelectorAll(selector, NodeOrElement.WrappedNode(dom.getDocument(depth = -1, pierce = true).root))

    private suspend fun Tab.oneOnTheDocument(selector: String): Element? =
        querySelector(selector, NodeOrElement.WrappedNode(dom.getDocument(depth = -1, pierce = true).root))

    private fun Element.seen() = listOf(tag, text, textAll, attrs.joinToString(), backendNodeId.toString())

    @Test
    fun select_findsWhatTheDocumentLookupFinds() = onPage {
        val before = oneOnTheDocument("li[data-rank='2']")!!

        val item = select("li[data-rank='2']", timeout = 5_000)

        assertEquals(before.seen(), item.seen())
        assertEquals(listOf("li", "Beta", "Beta", "class, data-rank"), item.seen().take(4))
        assertNotNull(item.objectId, "the element keeps the object the page returned")
    }

    @Test
    fun select_findsTheSameChildren() = onPage {
        val before = oneOnTheDocument("#list")!!.children.map { it.seen() }

        val children = select("#list", timeout = 5_000).children.map { it.seen() }

        assertEquals(before, children)
        assertEquals(3, children.count { it.first() == "li" })
    }

    @Test
    fun selectAll_findsTheSameElements_inTheSameOrder() = onPage {
        val before = allOnTheDocument(".item").map { it.seen() }

        val items = selectAll(".item", timeout = 5_000).map { it.seen() }

        assertEquals(before, items)
        assertEquals(listOf("Alpha", "Beta", "Gamma"), items.map { it[1] })
    }

    @Test
    fun noMatch_isNoMatch() = onPage {
        assertEquals(emptyList(), allOnTheDocument(".nowhere"))
        assertEquals(emptyList(), querySelectorAll(".nowhere"))
        assertNull(oneOnTheDocument(".nowhere"))
        assertNull(querySelector(".nowhere"))
    }

    @Test
    fun shadowRoots_areNotPierced() = onPage {
        assertEquals(emptyList(), allOnTheDocument("#shadowed"))
        assertEquals(emptyList(), querySelectorAll("#shadowed"))
        assertFailsWith<TimeoutWaitingForElementException> { select("#shadowed", timeout = 0) }
    }

    @Test
    fun frames_areNotSearched_butSelectAllWithFramesSearchesThem() = onPage {
        assertNull(oneOnTheDocument("p.inner"))
        assertNull(querySelector("p.inner"))
        val inFrames = selectAll("p.inner", timeout = 5_000, includeFrames = true)
        assertEquals(listOf("Inside the frame"), inFrames.map { it.text })
    }

    @Test
    fun aFoundElementCanBeClickedAndRead() = onPage {
        rawEvaluate(
            "document.getElementById('panel').onclick = () => document.getElementById('panel').dataset.state = 'open'"
        )

        select("#panel", timeout = 5_000).click()

        assertEquals("open", select("#panel", timeout = 5_000)["data-state"])
    }

    @Test
    fun anElementOfSelectAll_keepsNoObject_andCanBeClicked() = onPage {
        rawEvaluate("document.getElementById('label').onclick = (e) => e.target.textContent = 'Hit'")
        val label = selectAll("#label", timeout = 5_000).single()

        assertNull(label.objectId, "the objects of the lookup were released")
        label.click()

        assertEquals("Hit", select("#label", timeout = 5_000).text)
    }

    @Test
    fun selectAll_readsTheDocument_whenTheMatchesOutweighIt() = onPage {
        val before = allOnTheDocument("html, body").map { it.seen() }

        val outer = selectAll("html, body", timeout = 5_000)

        assertEquals(before, outer.map { it.seen() })
        assertTrue(outer.all { it.node.nodeId != 0 && it.tree != null }, "read from the document, as before")
    }

    @Test
    fun select_seesTheNewDocumentAfterANavigation() = onPage {
        rawEvaluate("document.getElementById('title').textContent = 'Changed'")
        assertEquals("Changed", select("#title", timeout = 5_000).text)

        reload()

        assertEquals("Lookup", select("#title", timeout = 5_000).text)
    }

    @Test
    fun anElementFoundInThePage_hasNoNodeIdTreeNorParent_untilUpdated() = onPage {
        val item = select("li[data-rank='3']", timeout = 5_000)

        assertEquals(0, item.node.nodeId)
        assertNull(item.parentId)
        assertNull(item.tree)
        assertFailsWith<RuntimeException> { item.parent }

        item.update()

        assertNotEquals(0, item.node.nodeId)
        assertEquals("ul", item.parent?.tag)
    }

    @Test
    fun update_givesTheNodeIdsTreeAndParent_theDocumentLookupGives() = onPage {
        selectAll("p.inner", timeout = 5_000, includeFrames = true) // the frame loaded: both reads see the same page
        // The same lookup on the whole document, read first: a later read gives every node a new id, so ids are
        // compared below on the document the update read, and everything else on this earlier read.
        val before = oneOnTheDocument("li[data-rank='3']")!!
        val item = select("li[data-rank='3']", timeout = 5_000)

        item.update()

        val tree = assertNotNull(item.tree)
        val onTheSameRead = querySelector("li[data-rank='3']", NodeOrElement.WrappedNode(tree))!!
        assertEquals(onTheSameRead.node, item.node, "node, subtree and node ids of the document read")
        assertEquals(onTheSameRead.parentId, item.parentId)
        assertEquals(onTheSameRead.parent!!.node, item.parent!!.node)
        assertEquals(item.node.nodeId, dom.querySelector(tree.nodeId, "li[data-rank='3']").nodeId, "a live node id")
        assertEquals(item.node.parentId, item.parent!!.node.nodeId)
        assertEquals(before.node.withoutIds(), item.node.withoutIds())
        assertEquals(before.parent!!.node.withoutIds(), item.parent!!.node.withoutIds())
        assertEquals(before.tree!!.withoutIds(), tree.withoutIds())
    }

    @Test
    fun aLookupUnderAnElementFoundInThePage_bindsItFirst() = onPage {
        val list = select("#list", timeout = 5_000)

        assertEquals("Gamma", list.querySelector("li[data-rank='3']")?.text)
        assertEquals(listOf("Alpha", "Beta", "Gamma"), list.querySelectorAll("li").map { it.text })
        assertNotNull(list.tree)
        assertEquals("body", list.parent?.tag)
    }

    @Test
    fun anInvalidSelector_throwsTheSameExceptionAsTheDocumentLookup() = onPage {
        for (selector in listOf("li[", "", "a:unknown-pseudo")) {
            val document = dom.getDocument(depth = -1, pierce = true).root
            val one = assertFailsWith<CDPException> { dom.querySelector(document.nodeId, selector) }
            val all = assertFailsWith<CDPException> { dom.querySelectorAll(document.nodeId, selector) }

            val ones = listOf<suspend () -> Unit>({ select(selector, timeout = 0) }, { querySelector(selector) })
            for (lookup in ones) assertSameError(one, assertFailsWith<CDPException> { lookup() })
            val alls = listOf<suspend () -> Unit>({ selectAll(selector, timeout = 0) }, { querySelectorAll(selector) })
            for (lookup in alls) assertSameError(all, assertFailsWith<CDPException> { lookup() })
        }
    }

    private fun assertSameError(expected: CDPException, actual: CDPException) {
        assertEquals(expected.method, actual.method)
        assertEquals(expected.code, actual.code)
        assertEquals(expected.originalMessage, actual.originalMessage)
        assertEquals(expected.data, actual.data)
        assertEquals(expected.message, actual.message)
    }

    // The node and its subtree with every node id and parent id cleared: what two reads of the same page share.
    private fun DOM.Node.withoutIds(): DOM.Node = copy(
        nodeId = 0,
        parentId = null,
        children = children?.map { it.withoutIds() },
        contentDocument = contentDocument?.withoutIds(),
        shadowRoots = shadowRoots?.map { it.withoutIds() },
        pseudoElements = pseudoElements?.map { it.withoutIds() },
        templateContent = templateContent?.withoutIds(),
        importedDocument = importedDocument?.withoutIds(),
    )

}
