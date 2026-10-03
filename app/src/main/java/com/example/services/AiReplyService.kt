package com.example.services

import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * AI Auto-Reply Service with Nigerian Pidgin + English conversion logic.
 * Convinces buyers, eliminates scam doubt with Trust Badge proofs, creates urgency.
 */
object AiReplyService {

    private const val TAG = "AiReplyService"
    private const val GEMINI_ENDPOINT =
        "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent"

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()

    suspend fun generateSmartReply(
        incomingMessage: String,
        businessName: String = "VendorOS Verified Shop",
        vendorState: String = "Lagos",
        products: String = "Original Quality Wears, Shoes & Accessories",
        overrideApiKey: String? = null
    ): String = withContext(Dispatchers.IO) {
        val apiKey = overrideApiKey ?: try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Exception) {
            ""
        }

        if (apiKey.isNullOrBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext fallbackSmartReply(incomingMessage, businessName, vendorState)
        }

        try {
            val systemPrompt = "You are $businessName sales assistant in $vendorState Nigeria. Customer says: '$incomingMessage'. Products: $products. Task: Understand intent (price/location/trust/buy), Reply persuasively in Pidgin+English mix short <30 words, Add urgency CTA Order now Delivery today, If price give price ask to order, If doubting mention Trust Score verified deliveries, Never mention account number. Say Pay via secure link. 1 emoji max."

            val jsonBody = JSONObject().apply {
                val contents = JSONArray().apply {
                    val contentObj = JSONObject().apply {
                        val parts = JSONArray().apply {
                            put(JSONObject().put("text", systemPrompt))
                        }
                        put("parts", parts)
                    }
                    put(contentObj)
                }
                put("contents", contents)
            }

            val request = Request.Builder()
                .url("$GEMINI_ENDPOINT?key=$apiKey")
                .post(jsonBody.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val response = httpClient.newCall(request).execute()
            if (response.isSuccessful) {
                val resStr = response.body?.string() ?: ""
                val resJson = JSONObject(resStr)
                val candidates = resJson.optJSONArray("candidates")
                if (candidates != null && candidates.length() > 0) {
                    val candidate = candidates.getJSONObject(0)
                    val content = candidate.optJSONObject("content")
                    val parts = content?.optJSONArray("parts")
                    val reply = parts?.getJSONObject(0)?.optString("text")?.trim()
                    if (!reply.isNullOrBlank()) {
                        return@withContext reply.take(280)
                    }
                }
            } else {
                Log.w(TAG, "Gemini API error: ${response.code} ${response.message}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed calling Gemini API", e)
        }

        return@withContext fallbackSmartReply(incomingMessage, businessName, vendorState)
    }

    fun fallbackSmartReply(
        incomingMessage: String,
        businessName: String = "Our Shop",
        state: String = "Lagos"
    ): String {
        val lower = incomingMessage.lowercase()
        return when {
            lower.contains("price") || lower.contains("cost") || lower.contains("how much") || lower.contains("hm") || lower.contains("amount") -> {
                "Yes boss! Available now. From ₦5,000, fast delivery today for $state. You wan order? I go reserve am for you 🙏"
            }
            lower.contains("where") || lower.contains("location") || lower.contains("address") || lower.contains("shop") || lower.contains("office") -> {
                "We dey $state, we dey deliver nationwide! Your area we fit deliver today. Make I confirm your location? 🚚"
            }
            lower.contains("scam") || lower.contains("trust") || lower.contains("fake") || lower.contains("real") || lower.contains("pay on delivery") || lower.contains("pod") -> {
                "No scam zone! Verified vendor with 100+ deliveries, check my Trust Badge. Your order safe with us ✅"
            }
            lower.contains("hello") || lower.contains("hi") || lower.contains("good day") || lower.contains("available") || lower.contains("still there") -> {
                "Welcome to $businessName! Yes, available and ready for dispatch today. Which color or size you need make I pack am for you? 📦"
            }
            lower.contains("pay") || lower.contains("transfer") || lower.contains("account") || lower.contains("send details") -> {
                "Boss, we use our official VendorOS encrypted secure link for zero-fraud transfers. I go send your order link now now! 🔒"
            }
            else -> {
                "Thank you for contacting $businessName! Order dispatching today for $state. How many pieces you wan make we package for you? ✨"
            }
        }
    }

    fun generateQuickReplies(businessName: String): List<String> {
        return listOf(
            "Available now boss! Delivery today, make I reserve am?",
            "No scam here! 100% verified with Trust Badge deliveries ✅",
            "We dey deliver nationwide. Which address make we send am?",
            "Secure payment only via encrypted link. Zero fraud guaranteed 🔒"
        )
    }
}
