package com.kolab.data

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Guest browsing must never look like a signed-in account: no tokens, no
 * cached profile, and signing in must leave guest mode behind.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class GuestSessionTest {

    private fun newTokenManager(): TokenManager {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val manager = TokenManager(context)
        manager.clearAll()
        return manager
    }

    @Test
    fun `guest session has no tokens and is not logged in`() {
        val manager = newTokenManager()

        manager.enterGuestMode()

        assertTrue(manager.isGuestMode())
        assertFalse(manager.isLoggedIn())
        assertNull(manager.getRefreshToken())
        assertNull(manager.getUserId())
        assertNull(manager.getUserName())
    }

    @Test
    fun `saving real tokens clears guest mode`() {
        val manager = newTokenManager()
        manager.enterGuestMode()

        manager.saveTokens("access-token", "refresh-token")

        assertFalse(manager.isGuestMode())
        assertTrue(manager.isLoggedIn())
    }

    @Test
    fun `clearing the session removes guest mode`() {
        val manager = newTokenManager()
        manager.enterGuestMode()

        manager.clearAll()

        assertFalse(manager.isGuestMode())
        assertFalse(manager.isLoggedIn())
    }

    @Test
    fun `legacy demo sessions are wiped on upgrade`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        SecurePrefs.create(context, "kolab_secure_prefs").edit()
            .putBoolean("demo_mode", true)
            .putString("refresh_token", "demo.refresh.token")
            .putString("user_name", "Demo User")
            .apply()

        val manager = TokenManager(context)

        assertFalse(manager.isGuestMode())
        assertFalse(manager.isLoggedIn())
        assertNull(manager.getUserName())
    }
}
