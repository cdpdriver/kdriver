---
title: Quickstart
parent: Home
nav_order: 1
---

# Quickstart

## Installation

To install, add the dependency to your `build.gradle.kts`:

```kotlin
dependencies {
    implementation("dev.kdriver:core:0.6.3")
}
```

## Basic usage

Open a browser, navigate to a page, and scrape the content:

```kotlin
fun main() = runBlocking {
    val browser = createBrowser(this)
    val page = browser.get("https://example.com")

    // Get HTML content of the page as a string
    val content = page.getContent()

    // Save a screenshot
    page.saveScreenshot()

    // Close the browser window
    browser.stop()
}
```

## More complete example

```kotlin
fun main() = runBlocking {
    val browser = createBrowser(this)
    val page2 = browser.get("https://twitter.com", newTab = true)
    val page3 = browser.get("https://github.com/cdpdriver/kdriver", newWindow = true)

    listOf(page2, page3).forEach { page ->
        page.bringToFront()
        page.scrollDown(200)
        page.wait()
        page.reload()
        if (page != page3) page.close()
    }
}
```

I'll leave out the boilerplate here

```kotlin
fun main() = runBlocking {
    val browser = createBrowser(
        this,
        headless = false,
        userDataDir = Path("/path/to/existing/profile"), // by specifying it, it won't be automatically cleaned up when finished
        browserExecutablePath = Path("/path/to/some/other/browser"),
        browserArgs = listOf("--some-browser-arg=true", "--some-other-option"),
        lang = "en-US", // this could set iso-language-code in navigator, not recommended to change
    )
    val tab = browser.get("https://somewebsite.com")

    // ...
}
```

## Alternative custom options

I'll leave out the boilerplate here

```kotlin
fun main() = runBlocking {
    val config = Config(
        headless = false,
        userDataDir = Path("/path/to/existing/profile"), // by specifying it, it won't be automatically cleaned up when finished
        browserExecutablePath = Path("/path/to/some/other/browser"),
        browserArgs = listOf("--some-browser-arg=true", "--some-other-option"),
        lang = "en-US", // this could set iso-language-code in navigator, not recommended to change
    )

    val browser = createBrowser(this, config)
    val tab = browser.get("https://somewebsite.com")

    // ...
}
```

A more concrete example, which can be found in the ./example/ folder,
shows a script for uploading an image to imgur.

```kotlin
fun main() = runBlocking {
    // Interesting, this is a typical site which runs completely on javascript, and that causes
    // this script to be faster than the js can present the elements. This may be one of the downsides
    // of this fast beast. You have to carefully consider timing.
    val DELAY = 2000L

    val browser = createBrowser(this)
    val tab = browser.get("https://imgur.com")

    // Now we first need an image to upload, lets make a screenshot of the project page
    val savePath = Path("screenshot.jpg")
    val tempTab = browser.get("https://github.com/cdpdriver/kdriver", newTab = true)

    // Wait page to load
    tempTab.wait()
    // Save the screenshot to the previously declared path of screenshot.jpg (which is just current directory)
    tempTab.saveScreenshot(savePath)
    // Done, discard the temp_tab
    tempTab.close()

    // Accept cookies
    // the best_match flag will filter the best match from
    // matching elements containing "consent" and takes the
    // one having most similar text length
    val consent = tab.find("Consent", bestMatch = true)
    consent.click()

    // Shortcut
    tab.find("new post", bestMatch = true).click()

    val fileInput = tab.select("input[type=file]")
    fileInput.sendFile(listOf(savePath))

    // Since file upload takes a while , the next buttons are not available yet
    tab.wait(DELAY)

    // Wait until the grab link becomes clickable, by waiting for the toast message
    tab.select(".Toast-message--check")

    // This one is tricky. We are trying to find a element by text content
    // usually. The text node itself is not needed, but it's enclosing element.
    // In this case however, the text is NOT a text node, but an "placeholder" attribute of a span element.
    // So for this one, we use the flag return_enclosing_element and set it to False
    val titleField = tab.find("give your post a unique title", bestMatch = true)
    println(titleField)
    titleField.sendKeys("kdriver")

    // There is a delay for the link sharing popup.
    // Let's pause for a sec
    val grabLink = tab.find("grab link", bestMatch = true)
    grabLink.click()

    tab.wait(DELAY)

    // Get inputs of which the value starts with http
    val inputThing = tab.select("input[value^=https]")
    val myLink = inputThing["value"]

    println(myLink)
    browser.stop()
}
```

## Connecting to a running browser

Instead of launching a browser, kdriver can attach to one that is already running — for instance a
browser exposed behind a reverse proxy, over TLS, under a path prefix and with authentication:

```kotlin
fun main() = runBlocking {
    val browser = createBrowser(this) {
        // http:// or https://, with an optional path prefix. The DevTools endpoints are requested
        // below it (https://browsers.example.com/team/one/json/version) and the WebSocket
        // connections use the same address with ws:// or wss://.
        browserUrl = "https://browsers.example.com/team/one"
        // Sent with every HTTP request and every WebSocket handshake to the browser.
        connectionHeaders = mapOf("Authorization" to "Bearer <token>")
    }
    val tab = browser.get("https://example.com")

    // ...

    // Let go of the browser without closing it: the WebSocket connections are closed, but the
    // browser and its tabs keep running. Use browser.stop() instead to shut it down.
    browser.detach()
}
```

The browser advertises its own, internal, address in `webSocketDebuggerUrl`; when `browserUrl` is
set, kdriver keeps only the path of that URL and connects to it on `browserUrl`. Without
`browserUrl`, `host` and `port` keep working as before.

## How lookups work

`select`, `selectAll`, `querySelector` and `querySelectorAll` (without a node) let the page run the
selector: kdriver evaluates `document.querySelector(…)` / `document.querySelectorAll(…)` in the page,
then asks the browser to describe only the matching elements, each with its subtree. The whole
document is never transferred, which matters most when the browser is far away (`browserUrl`) and
the page is large or keeps changing.

| What | How |
| --- | --- |
| Commands for `select` / `querySelector` | `Runtime.evaluate`, then `DOM.describeNode` of the match (one round trip when nothing matches) |
| Commands for `selectAll` / `querySelectorAll` | one `Runtime.evaluate` when nothing matches; otherwise also `Runtime.getProperties`, then every `DOM.describeNode` sent together, with the release of the objects the lookup created |
| Data received | the matches and their subtrees; when they would hold more elements than the whole document (nested matches, a selector matching most of the page), the document is read instead, as before |
| First call on a found element (`click`, `apply`, …) | `select` / `querySelector`: uses the object the page returned, no `DOM.resolveNode`; `selectAll` / `querySelectorAll`: resolves the node first, as before |
| Text, attributes, children, `backendNodeId` | read from the match and its subtree, as before |
| `element.node.nodeId`, `element.parentId`, `element.tree`, `element.parent` | 0, `null`, `null`, and `parent` throws, until `element.update()` reads the document; a lookup under the element does it by itself |
| Shadow roots, iframes | not searched (`selectAll(includeFrames = true)` reads the document to search inside each iframe, as before) |
| Waiting | `select` / `selectAll` retry every 500 ms until the timeout, as before |
| A navigation during the lookup | an object or context dropped by the navigation counts as no match: `select` tries again |
| Invalid selector | the same `CDPException` as before (`DOM.querySelector` or `DOM.querySelectorAll`, code -32000, "DOM Error while querying") |

```kotlin
val item = tab.select("li.item")
item.text            // read from the match
item.click()         // no extra command to resolve the element
item.tree            // null: no document was read
item.update()        // reads the document (one DOM.getDocument)
item.parent?.tag     // "ul"
```

What still differs from reading the whole document:

- the page's own `document.querySelector` runs, so a page that replaces it changes the result;
- the element found by `select` or `querySelector` holds the remote object of its match until the
  page navigates (an element read from the document held one only after its first call);
- until something reads the document (`find`, `update`, a lookup under an element…), the browser
  sends no `DOM.*` mutation events for the page, so a busy page may count as idle sooner, and the DOM
  agent is not enabled: a `tab.dom.disable()` sent by hand then fails, as on a tab that never read it;
- `element.node.nodeId`, `element.parentId`, `element.tree` and `element.parent` are plain
  properties: their first read cannot fetch the document by itself, so code that reads them calls
  `element.update()` first.

Lookups under an element (`element.querySelector`, `element.querySelectorAll`), `find`, `findAll`
and `xpath` read the document, as before.
