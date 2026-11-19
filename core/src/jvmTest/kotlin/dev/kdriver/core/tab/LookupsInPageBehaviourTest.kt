package dev.kdriver.core.tab

import dev.kdriver.cdp.CDPException
import dev.kdriver.core.browser.Browser
import dev.kdriver.core.browser.createBrowser
import dev.kdriver.core.exceptions.EvaluateException
import dev.kdriver.core.exceptions.TimeoutWaitingForElementException
import dev.kdriver.core.sampleFile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * On a real page, the same lookups with [dev.kdriver.core.browser.Config.lookupsInPage] off and on find the same
 * elements, with the same text, attributes, children and order, the same absence inside shadow roots and frames, and
 * elements that can be clicked; what differs is documented on the setting (no node id, no tree, the error type).
 */
class LookupsInPageBehaviourTest {

    private fun <T> onPage(lookupsInPage: Boolean, block: suspend Tab.() -> T): T = runBlocking {
        val browser: Browser = createBrowser(this as CoroutineScope) {
            headless = true
            sandbox = false
            this.lookupsInPage = lookupsInPage
        }
        try {
            browser.get(sampleFile("lookup-test.html")).block()
        } finally {
            browser.stop()
        }
    }

    /** Runs [block] with the setting off then on, and checks both give the same answer. */
    private fun <T> sameInBothModes(block: suspend Tab.() -> T): T {
        val off = onPage(lookupsInPage = false, block)
        val on = onPage(lookupsInPage = true, block)
        assertEquals(off, on)
        return on
    }

    @Test
    fun select_findsTheSameElement() {
        val seen = sameInBothModes {
            val item = select("li[data-rank='2']", timeout = 5_000)
            listOf(item.tag, item.text, item.textAll, item["data-rank"], item.attrs.joinToString())
        }
        assertEquals(listOf("li", "Beta", "Beta", "2", "class, data-rank"), seen)
    }

    @Test
    fun select_findsTheSameChildren() {
        val seen = sameInBothModes { select("#list", timeout = 5_000).children.filter { it.tag == "li" }.map { it.text } }
        assertEquals(listOf("Alpha", "Beta", "Gamma"), seen)
    }

    @Test
    fun selectAll_findsTheSameElements_inTheSameOrder() {
        val seen = sameInBothModes { selectAll(".item", timeout = 5_000).map { it.text to (it.backendNodeId > 0) } }
        assertEquals(listOf("Alpha" to true, "Beta" to true, "Gamma" to true), seen)
    }

    @Test
    fun querySelectorAll_returnsNothing_forNoMatch() {
        assertEquals(emptyList(), sameInBothModes { querySelectorAll(".nowhere").map { it.text } })
        assertNull(sameInBothModes { querySelector(".nowhere")?.text })
    }

    @Test
    fun select_doesNotPierceShadowRoots_inEitherMode() {
        sameInBothModes {
            assertFailsWith<TimeoutWaitingForElementException> { select("#shadowed", timeout = 0) }
            querySelectorAll("#shadowed").size
        }
    }

    @Test
    fun select_doesNotLookInsideFrames_butSelectAllWithFramesDoes_inEitherMode() {
        val seen = sameInBothModes {
            assertNull(querySelector("p.inner"))
            selectAll("p.inner", timeout = 5_000, includeFrames = true).map { it.text }
        }
        assertEquals(listOf("Inside the frame"), seen)
    }

    @Test
    fun aFoundElementCanBeClickedAndRead_inEitherMode() {
        val seen = sameInBothModes {
            rawEvaluate("document.getElementById('panel').onclick = () => document.getElementById('panel').dataset.state = 'open'")
            select("#panel", timeout = 5_000).click()
            select("#panel", timeout = 5_000)["data-state"]
        }
        assertEquals("open", seen)
    }

    @Test
    fun select_seesTheNewDocumentAfterANavigation_inEitherMode() {
        val seen = sameInBothModes {
            rawEvaluate("document.getElementById('title').textContent = 'Changed'")
            val before = select("#title", timeout = 5_000).text
            reload()
            before to select("#title", timeout = 5_000).text
        }
        assertEquals("Changed" to "Lookup", seen)
    }

    @Test
    fun withTheSettingOn_anElementHasNoTreeUntilUpdated() = onPage(lookupsInPage = true) {
        val item = select("li[data-rank='3']", timeout = 5_000)
        assertNull(item.tree)
        assertEquals(0, item.node.nodeId)
        assertNotNull(item.objectId, "the element keeps the object the page returned")
        assertFailsWith<RuntimeException> { item.parent }

        item.update()

        assertNotNull(item.tree)
        assertEquals("ul", item.parent?.tag)
    }

    @Test
    fun anInvalidSelector_throwsTheBrowserErrorOff_andThePageErrorOn() {
        onPage(lookupsInPage = false) { assertFailsWith<CDPException> { select("li[", timeout = 0) } }
        val error = onPage(lookupsInPage = true) { assertFailsWith<EvaluateException> { select("li[", timeout = 0) } }
        assertTrue(error.jsError.contains("SyntaxError"), error.jsError)
    }

}
