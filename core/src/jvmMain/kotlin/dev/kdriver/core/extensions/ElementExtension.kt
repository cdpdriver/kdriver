package dev.kdriver.core.extensions

import dev.kdriver.cdp.domain.Input
import dev.kdriver.core.dom.DefaultElement
import dev.kdriver.core.dom.Element
import dev.kdriver.core.dom.apply
import dev.kdriver.core.dom.pipelineInput
import kotlinx.coroutines.delay

/**
 * Types [text] into the element: printable ASCII as one `char` key event per character, anything else (accents, emoji,
 * combined characters) inserted grapheme cluster by grapheme cluster.
 *
 * The element is focused once, then the events are written back to back: the whole text costs about one round trip.
 *
 * @param text The text to type.
 */
suspend fun Element.sendKeysWithSpecialChars(text: String) {
    sendKeysWithSpecialChars(text) { 0L }
}

/**
 * Types [text] into the element like [sendKeysWithSpecialChars], waiting [delayBetweenClusters] milliseconds between two
 * grapheme clusters (a human typing pace, for instance).
 *
 * The element is focused once; then each cluster is written as soon as its delay is over, without waiting for the reply
 * of the previous one, so the page receives the keys at the pace of the delays whatever the latency of the link, and the
 * text costs about one round trip on top of the delays. Replies are all read at the end; an error that already came back
 * stops the typing at the next cluster.
 *
 * @param text The text to type.
 * @param delayBetweenClusters Called before each cluster but the first: the pause, in milliseconds, before it.
 * @throws dev.kdriver.cdp.CDPException the first error the browser answered.
 */
suspend fun Element.sendKeysWithSpecialChars(text: String, delayBetweenClusters: () -> Long) {
    apply<Unit>("(elem) => elem.focus()")
    val clusters = text.graphemeClusters()
    if (this !is DefaultElement) {
        // A custom Element: its own sendKeys / insertText, awaited one by one.
        clusters.forEachIndexed { i, cluster ->
            if (i > 0) delay(delayBetweenClusters())
            if (cluster.isPrintableAscii()) sendKeys(cluster) else insertText(cluster)
        }
        return
    }
    tab.pipelineInput {
        clusters.forEachIndexed { i, cluster ->
            if (i > 0) delay(delayBetweenClusters())
            if (cluster.isPrintableAscii()) {
                for (char in cluster) key(Input.DispatchKeyEventParameter(type = "char", text = char.toString()))
            } else insertText(cluster)
        }
    }
}

private fun String.isPrintableAscii(): Boolean = all { it.code in 32..126 }
