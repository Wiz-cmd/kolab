package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class KolabAppRobolectricTest {

  @Test
  fun `app name is Kolab`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    assertEquals("Kolab", context.getString(R.string.app_name))
  }

  @Test
  fun `base url is configured for the backend`() {
    assertFalse(BuildConfig.BASE_URL.isBlank())
  }
}