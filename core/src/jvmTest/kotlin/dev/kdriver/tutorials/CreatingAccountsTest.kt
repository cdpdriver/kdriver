package dev.kdriver.tutorials

import dev.kdriver.core.browser.createBrowser
import dev.kdriver.core.tab.Tab
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertTrue

class CreatingAccountsTest {

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

    suspend fun login(page: Tab, email: String, password: String): Boolean {
        // Fill in the login form
        page.select("#loginEmail").sendKeys(email)
        page.select("#loginPassword").sendKeys(password)

        // Click the "Login" button
        val loginButton = page.selectAll("button").firstOrNull { it.text.contains("Login") }
        loginButton!!.click()
        page.wait(500)

        // Verify successful login
        val message = page.select("#message")
        return message.textAll.contains("Welcome back")
    }

    @Test
    fun testCreatingAccounts() = runBlocking {
        val browser = createBrowser(this, headless = true, sandbox = false)
        val page = browser.get("https://cdpdriver.github.io/examples/login-page.html")
        page.wait(500) // Wait for the page to load

        val name = "John Doe"
        val email = "john.doe@example.com"
        val password = "securepassword"

        createAccount(page, name, email, password)
        assertTrue { login(page, email, password) }

        browser.stop()
    }

}
