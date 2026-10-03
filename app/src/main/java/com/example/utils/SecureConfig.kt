package com.example.utils

import android.util.Base64
import java.nio.charset.StandardCharsets
import java.security.MessageDigest

/**
 * VendorOS Secure Payment Configuration
 * SECURITY CRITICAL:
 * 1. NEVER display raw account in UI. Only maskedAccount ("7081****44").
 * 2. Obfuscated Base64 decode at runtime ONLY for API headers/network validation, never UI.
 * 3. All customer transfers go through server-side secure link (https://vendoros.ng/pay?ref=...).
 */
object SecureConfig {

    private fun decode(b64: String): String {
        return try {
            val bytes = Base64.decode(b64, Base64.DEFAULT)
            String(bytes, StandardCharsets.UTF_8)
        } catch (e: Exception) {
            ""
        }
    }

    val maskedAccount: String = "7081****44"
    val bankName: String = "OPay"
    val accountName: String = "VendorOS Payments"
    val supportWhatsApp: String = "2347081022844"
    val apiBase: String = "https://vendoros.ng/api"
    val securePayBase: String = "https://vendoros.ng/pay"

    fun getRealAccountForApi(): String {
        return try {
            val p1 = "NzA4"
            val p2 = "MTAy"
            val p3 = "Mjg0NA=="
            decode(p1) + decode(p2) + decode(p3)
        } catch (e: Exception) {
            ""
        }
    }

    val planPrices: Map<String, Int> = mapOf(
        "basic" to 1000,
        "pro" to 2000,
        "super" to 3000,
        "promote" to 1000,
        "handwork_pro" to 1500
    )

    val planNames: Map<String, String> = mapOf(
        "basic" to "Vendor Basic",
        "pro" to "Vendor Pro",
        "super" to "Super Mode",
        "promote" to "Shop Boost 7 Days",
        "handwork_pro" to "Handwork Pro"
    )

    fun generateSecureReference(plan: String, uid: String): String {
        val safeUid = if (uid.length >= 6) uid.substring(0, 6) else uid.padEnd(6, 'X')
        val raw = "VOS-${plan.uppercase()}-$safeUid-${System.currentTimeMillis()}"
        val bytes = raw.toByteArray(StandardCharsets.UTF_8)
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        val hex = digest.joinToString("") { "%02x".format(it) }
        val shortHex = if (hex.length >= 10) hex.substring(0, 10).uppercase() else hex.uppercase()
        return "VOS-$shortHex"
    }

    fun generateSecureLink(ref: String, amount: Int, plan: String): String {
        val token = Base64.encodeToString(ref.toByteArray(StandardCharsets.UTF_8), Base64.NO_WRAP)
        return "$securePayBase?ref=$ref&amount=$amount&plan=$plan&token=$token"
    }

    fun sha256(input: String): String {
        return try {
            val md = MessageDigest.getInstance("SHA-256")
            val digest = md.digest(input.toByteArray(StandardCharsets.UTF_8))
            digest.joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            input.hashCode().toString()
        }
    }
}
