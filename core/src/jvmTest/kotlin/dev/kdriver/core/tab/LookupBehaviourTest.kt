package dev.kdriver.core.tab

import dev.kdriver.cdp.CDPException
import dev.kdriver.cdp.domain.dom
import dev.kdriver.core.browser.Browser
import dev.kdriver.core.browser.createBrowser
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
 * What [Tab.select], [Tab.selectAll], [Tab.find], [Tab.findAll] and [Tab.querySelector] return on a real page, before
 * and after the page changes: the element, its text, attributes, tree and parent, the order of several results, the
 * best match of a text search, frames and shadow roots, and the errors. Every lookup has to see the page as it is when
 * it runs, whatever was looked up before.
 */
class LookupBehaviourTest {

    private fun withPage(block: suspend Tab.() -> Unit) = runBlocking {
        val browser: Browser = createBrowser(this as CoroutineScope, headless = true, sandbox = false)
        try {
            browser.get(sampleFile("lookup-test.html")).block()
        } finally {
            browser.stop()
        }
    }

    @Test
    fun select_returnsTheElementWithItsTextAttributesTreeAndParent() = withPage {
        val item = select("li[data-rank='2']", timeout = 5_000)

        assertEquals("li", item.tag)
        assertEquals("Beta", item.text)
        assertEquals("2", item["data-rank"])
        assertEquals(listOf("class", "data-rank"), item.attrs)
        assertNotNull(item.tree, "a looked-up element knows the document it is in")
        assertEquals("ul", item.parent?.tag)
        assertEquals("list", item.parent?.get("id"))
        assertEquals(listOf("Alpha", "Beta", "Gamma"), item.parent!!.children.filter { it.tag == "li" }.map { it.text })
    }

    @Test
    fun selectAll_returnsEveryMatchInDocumentOrder() = withPage {
        val items = selectAll(".item", timeout = 5_000)

        assertEquals(listOf("Alpha", "Beta", "Gamma"), items.map { it.text })
        assertEquals(listOf("1", "2", "3"), items.map { it["data-rank"] })
    }

    @Test
    fun selectAll_withFrames_findsInsideASameOriginFrame() = withPage {
        val inner = selectAll("p.inner", timeout = 5_000, includeFrames = true)

        assertEquals(listOf("Inside the frame"), inner.map { it.text })
    }

    @Test
    fun select_doesNotPierceShadowRoots() = withPage {
        assertFailsWith<TimeoutWaitingForElementException> { select("#shadowed", timeout = 0) }
    }

    @Test
    fun find_returnsTheBestMatch_orTheFirstOneWithoutBestMatch() = withPage {
        val best = find("Send", bestMatch = true, timeout = 5_000)
        val first = find("Send", bestMatch = false, timeout = 5_000)

        assertEquals("button", best.tag)
        assertEquals("p", first.tag)
    }

    @Test
    fun findAll_returnsEveryElementHoldingTheText() = withPage {
        val all = findAll("Send", timeout = 5_000)

        assertEquals(setOf("p", "button"), all.map { it.tag }.toSet())
    }

    @Test
    fun select_seesAnAttributeChangedSinceTheLastLookup() = withPage {
        assertEquals("closed", select("#panel", timeout = 5_000)["data-state"])

        rawEvaluate("document.getElementById('panel').setAttribute('data-state', 'open')")

        assertEquals("open", select("#panel", timeout = 5_000)["data-state"])
        assertEquals("panel", select("[data-state='open']", timeout = 5_000)["id"])
    }

    @Test
    fun select_seesAnAttributeRemovedSinceTheLastLookup() = withPage {
        assertEquals("closed", select("#panel", timeout = 5_000)["data-state"])

        rawEvaluate("document.getElementById('panel').removeAttribute('data-state')")

        assertNull(select("#panel", timeout = 5_000)["data-state"])
    }

    @Test
    fun select_seesTextChangedInPlaceSinceTheLastLookup() = withPage {
        assertEquals("one", select("#counter", timeout = 5_000).text)

        rawEvaluate("document.getElementById('counter').firstChild.nodeValue = 'two'")

        assertEquals("two", select("#counter", timeout = 5_000).text)
    }

    @Test
    fun select_seesTextReplacedSinceTheLastLookup() = withPage {
        assertEquals("one", select("#counter", timeout = 5_000).text)

        rawEvaluate("document.getElementById('counter').textContent = 'three'")

        assertEquals("three", select("#counter", timeout = 5_000).text)
    }

    @Test
    fun select_seesAnElementInsertedSinceTheLastLookup() = withPage {
        assertEquals("Panel", select("#label", timeout = 5_000).text)

        rawEvaluate(
            "(() => { const li = document.createElement('li'); li.className = 'item'; li.innerHTML = '<i>Delta</i>';" +
                " document.getElementById('list').appendChild(li); })()"
        )

        val delta = select("li.item:last-child", timeout = 5_000)
        assertEquals("Delta", delta.textAll)
        assertEquals("i", delta.children.single().tag)
        assertEquals(4, selectAll(".item", timeout = 5_000).size)
    }

    @Test
    fun select_doesNotReturnAnElementRemovedSinceTheLastLookup() = withPage {
        assertEquals("Panel", select("#label", timeout = 5_000).text)

        rawEvaluate("document.getElementById('label').remove()")

        assertFailsWith<TimeoutWaitingForElementException> { select("#label", timeout = 0) }
        assertNull(querySelector("#label"))
    }

    @Test
    fun select_seesTheNewDocumentAfterANavigation() = withPage {
        assertEquals("Lookup", select("#title", timeout = 5_000).text)

        rawEvaluate("document.getElementById('title').textContent = 'Changed'")
        reload()

        assertEquals("Lookup", select("#title", timeout = 5_000).text)
    }

    @Test
    fun select_afterFind_seesChangesMadeAfterTheFind() = withPage {
        assertEquals("button", find("Send", timeout = 5_000).tag)

        rawEvaluate("document.getElementById('panel').setAttribute('data-state', 'open')")

        assertEquals("open", select("#panel", timeout = 5_000)["data-state"])
    }

    @Test
    fun find_seesTextAddedSinceTheLastLookup() = withPage {
        assertEquals("Panel", select("#label", timeout = 5_000).text)

        rawEvaluate("document.getElementById('label').textContent = 'Opened panel'")

        assertEquals("span", find("Opened panel", timeout = 5_000).tag)
    }

    @Test
    fun update_afterALookup_keepsTheSameNodeAndTree() = withPage {
        val item = select("li[data-rank='3']", timeout = 5_000)

        item.update()

        assertEquals("Gamma", item.text)
        assertEquals("ul", item.parent?.tag)
        assertNotNull(item.objectId)
    }

    @Test
    fun repeatedLookups_onAnUnchangedPage_returnTheSameElements() = withPage {
        val first = selectAll(".item", timeout = 5_000)
        val second = selectAll(".item", timeout = 5_000)

        assertEquals(first.map { it.backendNodeId }, second.map { it.backendNodeId })
        assertEquals(first.map { it.text }, second.map { it.text })
    }

    @Test
    fun select_withAnInvalidSelector_throwsTheBrowserError() = withPage {
        assertFailsWith<CDPException> { select("li[", timeout = 0) }
    }

    @Test
    fun lookupsKeepWorking_afterTheDomAgentWasDisabledByHand() = withPage {
        assertEquals("Alpha", select("li[data-rank='1']", timeout = 5_000).text)

        dom.disable()
        rawEvaluate("document.querySelector(\"li[data-rank='1']\").textContent = 'Omega'")

        assertEquals("Omega", select("li[data-rank='1']", timeout = 5_000).text)
    }

    @Test
    fun lookupsKeepWorking_afterTheDocumentWasReadByHand() = withPage {
        assertEquals("Alpha", select("li[data-rank='1']", timeout = 5_000).text)

        dom.getDocument(depth = 1)

        val again = select("li[data-rank='1']", timeout = 5_000)
        assertEquals("Alpha", again.text)
        assertTrue(again.tree != null)
    }
}
