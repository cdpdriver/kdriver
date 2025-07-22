package dev.kdriver.core.connection

import dev.kdriver.core.browser.Browser

interface OwnedConnection : Connection {

    var owner: Browser?

}
