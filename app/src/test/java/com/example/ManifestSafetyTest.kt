package com.example

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory

/**
 * Regression test for the startup crash where Sentry's bundled
 * SentryInitProvider initialised before KolabApplication and threw
 * "DSN is required" whenever SENTRY_DSN was empty.
 *
 * Auto-init must stay disabled; the app initialises Sentry itself and only
 * when a DSN is actually configured.
 */
class ManifestSafetyTest {

    @Test
    fun `sentry auto init is disabled`() {
        val manifest = File("src/main/AndroidManifest.xml")
        assertTrue("AndroidManifest.xml not found at ${manifest.absolutePath}", manifest.exists())

        val document = DocumentBuilderFactory.newInstance()
            .newDocumentBuilder()
            .parse(manifest)

        val metaData = document.getElementsByTagName("meta-data")
        var autoInit: String? = null
        for (i in 0 until metaData.length) {
            val element = metaData.item(i)
            val name = element.attributes.getNamedItem("android:name")?.nodeValue
            if (name == "io.sentry.auto-init") {
                autoInit = element.attributes.getNamedItem("android:value")?.nodeValue
                break
            }
        }

        assertEquals(
            "io.sentry.auto-init must be false, otherwise the app crashes at launch when SENTRY_DSN is empty",
            "false",
            autoInit,
        )
    }
}
