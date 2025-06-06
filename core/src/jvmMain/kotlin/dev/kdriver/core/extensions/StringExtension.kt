package dev.kdriver.core.extensions

import java.text.BreakIterator

fun String.graphemeClusters(): List<String> = BreakIterator.getCharacterInstance().let { breakIterator ->
    breakIterator.setText(this)
    val result = mutableListOf<String>()
    var start = breakIterator.first()
    var end = breakIterator.next()
    while (end != BreakIterator.DONE) {
        result.add(this.substring(start, end))
        start = end
        end = breakIterator.next()
    }
    result
}
