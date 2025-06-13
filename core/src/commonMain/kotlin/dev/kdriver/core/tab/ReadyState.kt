package dev.kdriver.core.tab

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents the ready state of a document in a web browser.
 *
 * See https://developer.mozilla.org/en-US/docs/Web/API/Document/readyState
 */
@Serializable
enum class ReadyState {

    /**
     * The document is still loading.
     */
    @SerialName("loading")
    LOADING,

    /**
     * The document has finished loading and the document has been parsed but sub-resources such as scripts, images, stylesheets and frames are still loading. The state indicates that the DOMContentLoaded event is about to fire.
     */
    @SerialName("interactive")
    INTERACTIVE,

    /**
     * The document and all sub-resources have finished loading. The state indicates that the load event is about to fire.
     */
    @SerialName("complete")
    COMPLETE,

}
