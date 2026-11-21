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

/** The replies and helpers the tests of the lookups in the page share. */
abstract class LookupsInPageFixtures {

    protected val button =
        """{"node":{"nodeId":0,"backendNodeId":70,"nodeType":1,"nodeName":"BUTTON","localName":"button",""" +
            """"nodeValue":"","attributes":["id","go"],"childNodeCount":1,"children":[{"nodeId":0,""" +
            """"backendNodeId":80,"nodeType":3,"nodeName":"#text","localName":"","nodeValue":"Send"}]}}"""

    protected fun item(backendNodeId: Int, text: String) =
        """{"node":{"nodeId":0,"backendNodeId":$backendNodeId,"nodeType":1,"nodeName":"LI","localName":"li",""" +
            """"nodeValue":"","childNodeCount":1,"children":[{"nodeId":0,"backendNodeId":${backendNodeId + 1},""" +
            """"nodeType":3,"nodeName":"#text","localName":"","nodeValue":"$text"}]}}"""

    // An own property of the array of `querySelectorAll`: the match at [index], as Runtime.getProperties lists it.
    protected fun match(index: Int, objectId: String) =
        """{"name":"$index","configurable":true,"enumerable":true,""" +
            """"value":{"type":"object","subtype":"node","objectId":"$objectId"}}"""

    protected val nothing = """{"result":{"type":"object","subtype":"null","value":null}}"""

    protected fun found(objectId: String, subtype: String = "node") =
        """{"result":{"type":"object","subtype":"$subtype","objectId":"$objectId"}}"""

    protected val document =
        """{"root":{"nodeId":1,"backendNodeId":1,"nodeType":9,"nodeName":"#document","localName":"","nodeValue":"",""" +
            """"childNodeCount":1,"children":[{"nodeId":7,"backendNodeId":70,"nodeType":1,"nodeName":"BUTTON",""" +
            """"localName":"button","nodeValue":"","attributes":["id","go"],"childNodeCount":1,"children":[""" +
            """{"nodeId":8,"backendNodeId":80,"nodeType":3,"nodeName":"#text","localName":"","nodeValue":"Send"}]}]}}"""

    protected fun TestScope.tab(answer: (ScriptedTransport.Sent) -> Reply): ScriptedTab =
        ScriptedTab(this, ScriptedTransport(this, answer))

    protected val ScriptedTab.sent get() = transport.methods

    protected val arrayOfOne = """{"result":[""" +
        """${match(0, "li-0")}""" +
        """]}"""
}
