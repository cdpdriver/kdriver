---
title: Creating accounts
parent: Tutorials
nav_order: 12
---

# Creating accounts

**Target page:**
[https://slensky.com/zendriver-examples/login-page.html](https://slensky.com/zendriver-examples/login-page.html)

In this tutorial, we will demonstrate how to fill out a new account sign-up form and then log in with the newly created
account. The example page login/signup is implemented entirely with JavaScript, so created accounts do not persist once
the tab has been closed.

Feel free to open the page now in your current browser to get an idea of what we will be working with!

## Initial setup

Begin by creating a new script for the tutorial:

```kotlin
fun main() = runBlocking {
    val browser = Browser.create(this)
    val page = browser.get("https://slensky.com/zendriver-examples/login-page.html")

    // TODO: TODO: Sign-up and login

    browser.stop()
}
```

## Creating a new account

In this example page, you can create a new account by clicking on the "Sign up" link, which makes the sign-up form
visible when clicked.

We can create a new function to click this link, fill out the form, and submit it:

```kotlin
suspend fun createAccount(page: Tab, name: String, email: String, password: String) {
    // Click on the "Sign up" link
    val signUpLink = page.selectAll("a").firstOrNull { it.text.contains("Sign up") }
    signUpLink!!.click()
    page.wait(500)

    // Fill in the sign-up form
    page.select("#signupName").sendKeys(name)
    page.select("#signupEmail").sendKeys(email)
    page.select("#signupPassword").sendKeys(password)

    // Click the "Sign Up" button
    val signUpButton = page.selectAll("button").firstOrNull { it.text.contains("Sign Up") }
    signUpButton!!.click()
    page.wait(500)

    // Click through confirmation dialog
    val proceedToLogin = page.find("Proceed to Login")
    proceedToLogin.click()
}
```

## Logging in

Next, filling out the login form and logging in:

```kotlin
suspend fun login(page: Tab, email: String, password: String) {
    // Fill in the login form
    page.select("#loginEmail").sendKeys(email)
    page.select("#loginPassword").sendKeys(password)

    // Click the "Login" button
    val loginButton = page.selectAll("button").firstOrNull { it.text.contains("Login") }
    loginButton!!.click()
    page.wait(500)

    // Verify successful login
    val message = page.select("#message")
    if (message.textAll.contains("Welcome back")) println("Login successful")
    else println("Login failed")
}
```

## Putting it all together

```kotlin
suspend fun createAccount(page: Tab, name: String, email: String, password: String) {
    // Click on the "Sign up" link
    val signUpLink = page.selectAll("a").firstOrNull { it.text.contains("Sign up") }
    signUpLink!!.click()
    page.wait(500)

    // Fill in the sign-up form
    page.select("#signupName").sendKeys(name)
    page.select("#signupEmail").sendKeys(email)
    page.select("#signupPassword").sendKeys(password)

    // Click the "Sign Up" button
    val signUpButton = page.selectAll("button").firstOrNull { it.text.contains("Sign Up") }
    signUpButton!!.click()
    page.wait(500)

    // Click through confirmation dialog
    val proceedToLogin = page.find("Proceed to Login")
    proceedToLogin.click()
}

suspend fun login(page: Tab, email: String, password: String) {
    // Fill in the login form
    page.select("#loginEmail").sendKeys(email)
    page.select("#loginPassword").sendKeys(password)

    // Click the "Login" button
    val loginButton = page.selectAll("button").firstOrNull { it.text.contains("Login") }
    loginButton!!.click()
    page.wait(500)

    // Verify successful login
    val message = page.select("#message")
    if (message.textAll.contains("Welcome back")) println("Login successful")
    else println("Login failed")
}

fun main() = runBlocking {
    val browser = Browser.create(this)
    val page = browser.get("https://slensky.com/zendriver-examples/login-page.html")
    page.wait(500) // Wait for the page to load

    val name = "John Doe"
    val email = "john.doe@example.com"
    val password = "securepassword"

    createAccount(page, name, email, password)
    login(page, email, password)

    browser.stop()
}
```
