package com.kolab.data.api

import org.junit.Assert.assertEquals
import org.junit.Test

class ApiClientBaseUrlTest {

    @Test
    fun `blank base url falls back to production`() {
        assertEquals(ApiClient.FALLBACK_BASE_URL, ApiClient.normalizeBaseUrl(""))
        assertEquals(ApiClient.FALLBACK_BASE_URL, ApiClient.normalizeBaseUrl("   "))
    }

    @Test
    fun `malformed values fall back instead of crashing`() {
        assertEquals(ApiClient.FALLBACK_BASE_URL, ApiClient.normalizeBaseUrl("not-a-url"))
        assertEquals(ApiClient.FALLBACK_BASE_URL, ApiClient.normalizeBaseUrl("/api/"))
        assertEquals(ApiClient.FALLBACK_BASE_URL, ApiClient.normalizeBaseUrl("ftp://example.com"))
    }

    @Test
    fun `valid urls keep their value and gain a trailing slash`() {
        assertEquals("http://10.0.2.2:3000/api/", ApiClient.normalizeBaseUrl("http://10.0.2.2:3000/api"))
        assertEquals(
            "https://kolab-api-7r6i.onrender.com/api/",
            ApiClient.normalizeBaseUrl("https://kolab-api-7r6i.onrender.com/api/"),
        )
    }
}
