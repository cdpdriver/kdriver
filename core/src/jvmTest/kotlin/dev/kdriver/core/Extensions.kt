package dev.kdriver.core

fun sampleFile(name: String): String {
    val resource = checkNotNull(Thread.currentThread().contextClassLoader.getResource(name)) {
        "Resource '$name' not found"
    }
    return resource.toURI().toString()
}
