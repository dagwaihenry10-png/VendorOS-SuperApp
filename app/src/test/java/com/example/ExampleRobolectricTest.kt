package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.services.AiReplyService
import com.example.utils.SecureConfig
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("VendorOS", appName)
    }

    @Test
    fun `verify secure config obfuscation and masking`() {
        assertEquals("7081****44", SecureConfig.maskedAccount)
        // Verify real account decode works for server header only
        val apiAccount = SecureConfig.getRealAccountForApi()
        assertEquals("7081022844", apiAccount)

        val ref = SecureConfig.generateSecureReference("pro", "user123")
        assertTrue(ref.startsWith("VOS-"))
    }

    @Test
    fun `verify smart fallback replies in pidgin and english`() {
        val priceReply = AiReplyService.fallbackSmartReply("How much is this?", "Classic Wears", "Lagos")
        assertTrue(priceReply.contains("5000") || priceReply.contains("₦"))

        val scamReply = AiReplyService.fallbackSmartReply("Are you scam?", "Classic Wears", "Lagos")
        assertTrue(scamReply.contains("Trust Badge") || scamReply.contains("Verified"))
    }
}
