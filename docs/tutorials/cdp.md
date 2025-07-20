---
title: Playing with CDP
parent: Tutorials
nav_order: 14
---

# Playing with CDP

**Target page:**
[https://cdpdriver.github.io/examples/console.html](https://cdpdriver.github.io/examples/console.html)

In this tutorial, we will explore how to use the Chrome DevTools Protocol (CDP) directly, when kdriver does not provide
a specific API for the functionality you need.

## Sending CDP commands

There are various ways to send CDP commands, depending on what you prefer (direct, zendriver-like, ...). They are all
equivalent and do the same thing, so you can choose the one that suits you best.

```kotlin
fun main() = runBlocking {
    val browser = createBrowser(this)
    val page = browser.get("https://cdpdriver.github.io/examples/console.html")

    // Those 4 lines are equivalent and do the same thing
    page.runtime.enable()
    page.cdp.runtime.enable()
    page.send { runtime.enable() }
    page.send { cdp.runtime.enable() }

    // More coming...

    browser.stop()
}
```

## Listening to CDP events

Now, let's listen to some CDP events. In this example, we will listen to console messages and print them out.

```kotlin
fun main() = runBlocking {
    val browser = createBrowser(this)
    val page = browser.get("https://cdpdriver.github.io/examples/console.html")

    // Those 4 lines are equivalent and do the same thing
    page.runtime.enable()
    page.cdp.runtime.enable()
    page.send { runtime.enable() }
    page.send { cdp.runtime.enable() }

    // Create a listener for console API calls
    val consoleHandler: (Runtime.ConsoleAPICalledParameter) -> Unit = { event ->
        println("${event.type} - ${event.args.joinToString(", ") { it.value.toString() }}")
    }

    // Those 4 lines are equivalent and do the same thing
    val job1 = launch { page.runtime.consoleAPICalled.collect { consoleHandler(it) } }
    val job2 = launch { page.cdp.runtime.consoleAPICalled.collect { consoleHandler(it) } }
    val job3 = page.addHandler(this, { runtime.consoleAPICalled }, consoleHandler)
    val job4 = page.addHandler(this, { cdp.runtime.consoleAPICalled }, consoleHandler)

    page.select("#myButton").click()

    page.wait(1000) // Wait for the console messages to be printed

    // Remember to cancel the job to stop listening to console events
    job1.cancel()
    job2.cancel()
    job3.cancel()
    job4.cancel()

    browser.stop()
}
```
