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

## Lookups on a remote browser

By default, `select`, `selectAll`, `querySelector` and `querySelectorAll` read the whole document
(`DOM.getDocument`) each time the page changed since the last read, then query it. That read is the
size of the whole DOM, often a megabyte, and it happens after every navigation and on any page that
keeps changing. When the browser is far away, it is usually the largest transfer of a script.

With `lookupsInPage`, these lookups ask the page instead: the page runs the selector and only the
matching elements are sent back.

```kotlin
val browser = createBrowser(this) {
    browserUrl = "https://browsers.example.com/team/one"
    lookupsInPage = true
}
```

The setting is off by default. What changes when it is on:

| | Off (default) | On |
| --- | --- | --- |
| Commands for `select` / `querySelector` | `DOM.getDocument` (whole document, unless the page has not changed since the last read), then `DOM.querySelector` | `Runtime.evaluate` (`document.querySelector`), then `DOM.describeNode` of the match |
| Commands for `selectAll` / `querySelectorAll` | `DOM.getDocument`, then `DOM.querySelectorAll` | one `Runtime.evaluate` when nothing matches; otherwise also `Runtime.getProperties`, then one `DOM.describeNode` per match, sent together |
| Data received per lookup | the whole document | the matches and their subtrees (many or nested matches can add up to more than the document) |
| First call on a found element (`click`, `apply`, …) | resolves the node first (`DOM.resolveNode`) | uses the object the page returned, no extra command |
| `element.node.nodeId` | the node id | 0: no document was requested |
| `element.tree`, `element.parent` | the document, the parent | `null`, and `parent` throws until `element.update()`, which reads the document |
| Text, attributes, children of an element | read from the node and its subtree | the same |
| Shadow roots | not searched | not searched |
| Iframes | not searched; `selectAll(includeFrames = true)` searches inside each one | the same: the iframes are found in the page, then the document is read to search inside each one |
| Waiting | `select` / `selectAll` retry every 500 ms until the timeout | the same; with no document requested, the browser sends fewer `DOM.*` events, so a busy page may count as idle sooner |
| Invalid selector | `CDPException` | `EvaluateException` (the page's `SyntaxError`) |
| A page that replaces `document.querySelector` | not affected | the page's own function runs |

Lookups under an element (`element.querySelector`, `element.querySelectorAll`), `find`, `findAll`
and `xpath` are the same in both modes: they still read the document.
