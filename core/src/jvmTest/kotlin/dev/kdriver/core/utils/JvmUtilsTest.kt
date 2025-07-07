package dev.kdriver.core.utils

import dev.kdriver.core.network.EncodedBody
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.test.Test
import kotlin.test.assertEquals

class JvmUtilsTest {

    @OptIn(ExperimentalEncodingApi::class)
    @Test
    fun testDecompressZstd() {
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

}
