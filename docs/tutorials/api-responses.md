---
title: Reading API responses
parent: Tutorials
nav_order: 13
---

# Reading API responses

**Target page:**
[https://slensky.com/zendriver-examples/api-request.html](https://slensky.com/zendriver-examples/api-request.html)

In this tutorial, we will demonstrate how to read a dynamically loaded API response using response expectations.

The example page simulates an API request by waiting for a few seconds and then fetching a static JSON file. While it
would be far easier in this case to just fetch the JSON file directly, for demonstration purposes, let's instead pretend
that the response comes from a more complex API that cannot easily be called directly.

## Initial setup

Begin by creating a new script for the tutorial:

```kotlin
fun main() = runBlocking {
    val browser = Browser.create(this)
    // TODO: Read the API response
    val page = browser.get("https://slensky.com/zendriver-examples/api-request.html")

    browser.stop()
}
```

## Reading the API response

```kotlin
fun main() = runBlocking {
    val browser = Browser.create(this)

    val page = browser.mainTab ?: return@runBlocking
    val response = page.expect(Regex(".*/user-data.json")) {
        page.get("https://slensky.com/zendriver-examples/api-request.html")
        getResponseEvent()
    }

    val requestId = response.requestId
    val body = page.network.getResponseBody(requestId).body
    val userData = Serialization.json.parseToJsonElement(body)

    println("Successfully read user data response for user: " + userData.jsonObject["name"]?.jsonPrimitive?.content)
    println(Serialization.json.encodeToString(userData))

    browser.stop()
}
```

Or in a simpler way, if you only need the response body:

```kotlin
fun main() = runBlocking {
    val browser = Browser.create(this)

    val page = browser.mainTab ?: return@runBlocking
    val body = page.expect(Regex(".*/user-data.json")) {
        page.get("https://slensky.com/zendriver-examples/api-request.html")
        getResponseBody().body
    }
    val userData = Serialization.json.parseToJsonElement(body)

    println("Successfully read user data response for user: " + userData.jsonObject["name"]?.jsonPrimitive?.content)
    println(Serialization.json.encodeToString(userData))

    browser.stop()
}
```
