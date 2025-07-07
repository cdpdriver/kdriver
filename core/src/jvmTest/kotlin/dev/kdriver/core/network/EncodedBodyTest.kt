package dev.kdriver.core.network

import kotlin.test.Test
import kotlin.test.assertEquals

class EncodedBodyTest {

    @Test
    fun testDecompressPlain() {
        val compressedData = EncodedBody(
            """
            {
              "userId": 1,
              "id": 1,
              "title": "delectus aut autem",
              "completed": false
            }
            """.trimIndent(),
            false
        )
        assertEquals(
            """
            {
              "userId": 1,
              "id": 1,
              "title": "delectus aut autem",
              "completed": false
            }
            """.trimIndent(),
            compressedData.decodedBody
        )
    }

    @Test
    fun testDecompressBase64() {
        val compressedData = EncodedBody(
            "ewogICJ1c2VySWQiOiAxLAogICJpZCI6IDEsCiAgInRpdGxlIjogImRlbGVjdHVzIGF1dCBhdXRlbSIsCiAgImNvbXBsZXRlZCI6IGZhbHNlCn0=",
            true
        )
        assertEquals(
            """
            {
              "userId": 1,
              "id": 1,
              "title": "delectus aut autem",
              "completed": false
            }
            """.trimIndent(),
            compressedData.decodedBody
        )
    }

    @Test
    fun testDecompressZstdBase64() {
        val compressedData = EncodedBody(
            "KLUv/QRYJAIAQkQOFKC1OUcguaszR9dJ8Pa93RQSVcJgAeJlhmAp8m2GKk6Vf4Y/w3U/NqOy1Pb0VJIEAkvRcHhdSQIIAgA8JVxvhQkBAAAXn6EO",
            true
        )
        assertEquals(
            """
            {
              "userId": 1,
              "id": 1,
              "title": "delectus aut autem",
              "completed": false
            }
            """.trimIndent(),
            compressedData.decodedBody
        )
    }

    @Test
    fun testDecompressGzipBase64() {
        val compressedData = EncodedBody(
            "H4sIAPLIa2gAA6vmUlBQKi1OLfJMUbJSMNQBcTMRzJLMkpxUIE8pJTUnNbmktFghsbQEhFNzlcAKkvNzC3JSS1JBWtISc4pTuWoBN9wBt1MAAAA=",
            true
        )
        assertEquals(
            """
            {
              "userId": 1,
              "id": 1,
              "title": "delectus aut autem",
              "completed": false
            }
            """.trimIndent(),
            compressedData.decodedBody
        )
    }

}
