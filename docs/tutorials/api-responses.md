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

We'll also need a data class to represent the user data we expect to receive from the API:

```kotlin
@Serializable
data class UserData(
    val name: String,
    val title: String,
    val email: String,
    val location: String,
    val avatar: String,
    val bio: String,
    val skills: List<String>,
)
```

## Reading the API response

```kotlin
fun main() = runBlocking {
    val browser = Browser.create(this)

    val page = browser.mainTab ?: return@runBlocking
    val userData = page.expect(Regex(".*/user-data.json")) {
        page.get("https://slensky.com/zendriver-examples/api-request.html")
        getResponseBody<UserData>() // Wait and decode the response body from the matching expectation
    }

    println("Successfully read user data response for user: " + userData.name)
    println(Serialization.json.encodeToString(userData))

    browser.stop()
}
```
