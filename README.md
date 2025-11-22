# kdriver

[![License](https://img.shields.io/github/license/cdpdriver/kdriver)](LICENSE)
[![Maven Central Version](https://img.shields.io/maven-central/v/dev.kdriver/core)](https://klibs.io/project/cdpdriver/kdriver)
[![Issues](https://img.shields.io/github/issues/cdpdriver/kdriver)]()
[![Pull Requests](https://img.shields.io/github/issues-pr/cdpdriver/kdriver)]()
[![codecov](https://codecov.io/github/cdpdriver/kdriver/branch/main/graph/badge.svg?token=F7K641TYFZ)](https://codecov.io/github/cdpdriver/kdriver)
[![CodeFactor](https://www.codefactor.io/repository/github/cdpdriver/kdriver/badge)](https://www.codefactor.io/repository/github/cdpdriver/kdriver)
[![Open Source Helpers](https://www.codetriage.com/cdpdriver/kdriver/badges/users.svg)](https://www.codetriage.com/cdpdriver/kdriver)

> This project is a Kotlin port of Python library [zendriver](https://github.com/cdpdriver/zendriver), built to bring
> the same simplicity and power of CDP-based automation to the Kotlin and Java/JVM world.

kdriver is a blazing fast, coroutine-first, undetectable web scraping / browser automation library for Kotlin. It uses
the Chrome DevTools Protocol (CDP) under the hood to interact with real Chrome instances, offering a sleek alternative
to heavy tools like Selenium or Puppeteer — all without relying on WebDriver.

* **Documentation:** [kdriver.dev](https://kdriver.dev)
* **AI-generated wiki:** [deepwiki.com/cdpdriver/kdriver](https://deepwiki.com/cdpdriver/kdriver)
* **Repository:** [github.com/cdpdriver/kdriver](https://github.com/cdpdriver/kdriver)
* **Code coverage:** [codecov.io/github/cdpdriver/kdriver](https://codecov.io/github/cdpdriver/kdriver)

## Features

* **Undetectable** – Like its Python counterpart, kdriver speaks CDP directly, making it (almost) impossible for sites
  to detect.
* **Blazing fast** – CDP is lightweight and extremely performant. Combined with Kotlin coroutines, you get low-latency
  scraping and automation.
* **Simple and powerful API** – Designed to get you up and running with just a few lines of idiomatic Kotlin.
* **No external dependencies** – kdriver interacts directly with the browser using WebSockets and JSON — no need for
  Chromium wrappers or native libraries.
* **Session and cookie management** – Start with a clean profile every time or load/save cookies between runs.
* **Element utilities** – Coming soon: intuitive APIs to query, interact with, and wait for DOM elements.

## Motivation

We chose Kotlin for this port because:

* **First-class type safety and null safety** help catch bugs early and make the code more robust.
* **It's the core language of our entire stack** (backends, Android apps, shared libraries).
* **Multiplatform support** is on the roadmap, allowing future compatibility with native and JS targets. *(Currently
  JVM-only)*

## Installation

To use kdriver, add the following to your `build.gradle.kts`:

```kotlin
dependencies {
    implementation("dev.kdriver:core:0.5.0")
}
```

Make sure you have Maven Central configured:

```kotlin
repositories {
    mavenCentral()
}
```

## Usage

Visit a website and do something on it:

```kotlin
fun main() = runBlocking {
    val browser = createBrowser(this)
    val page = browser.get("https://www.browserscan.net/bot-detection")
    page.saveScreenshot(Path("browserscan.png"))
    browser.stop()
}
```

More examples can be found on the [tutorial section of the documentation](https://kdriver.dev/tutorials/).

## Acknowledgments

Huge thanks to [@stephanlensky](https://github.com/stephanlensky) for
creating [zendriver](https://github.com/cdpdriver/zendriver), a brilliantly designed and maintained library that
inspired this port.
If you're using Python, we highly recommend checking out the original project!

## Goals of the project

The purpose of **kdriver** is to provide a clean, coroutine-native API for browser automation in Kotlin, while staying
true to the goals of zendriver:

1. Stay undetected in modern anti-bot environments
2. Offer a developer-friendly experience with minimal setup
3. Empower developers with a flexible, low-level browser interface

This library is still in early development — feedback and contributions are very welcome!

## Contributing

We welcome contributions of all kinds — feel free to open issues, report bugs, or submit pull requests.
